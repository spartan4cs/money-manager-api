import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { Transaction, TransactionType } from '../../models/transaction.model';
import { Account } from '../../models/account.model';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, DatePipe, FormsModule],
  templateUrl: './transactions.html',
  styleUrl: './transactions.css'
})
export class Transactions implements OnInit {
  transactions = signal<Transaction[]>([]);
  accounts = signal<Account[]>([]);
  isLoading = signal<boolean>(true);

  isCreating = signal<boolean>(false);
  newTransaction: Partial<Transaction> = {
    type: 'EXPENSE',
    amount: 0,
    description: '',
    accountId: undefined,
    sourceAccountId: undefined,
    destinationAccountId: undefined
  };

  constructor(
    private apiService: ApiService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    // Check if we came from a quick link parameter
    this.route.queryParams.subscribe(params => {
      if (params['action']) {
        const actionType = params['action'].toUpperCase();
        if (['INCOME', 'EXPENSE', 'TRANSFER'].includes(actionType)) {
          this.isCreating.set(true);
          this.newTransaction.type = actionType as TransactionType;
        }
      }
    });

    this.loadData();
  }

  loadData(): void {
    this.isLoading.set(true);

    // Load accounts for the form selectors
    this.apiService.getAccounts().subscribe({
      next: (accs) => this.accounts.set(accs),
      error: (err) => console.error(err)
    });

    // Load transactions
    this.apiService.getTransactions().subscribe({
      next: (data) => {
        // Sort newest first
        const sorted = data.sort((a, b) => {
          const dateA = a.dateTime ? new Date(a.dateTime).getTime() : 0;
          const dateB = b.dateTime ? new Date(b.dateTime).getTime() : 0;
          return dateB - dateA;
        });
        this.transactions.set(sorted);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error fetching transactions', err);
        this.isLoading.set(false);
      }
    });
  }

  toggleCreateForm(): void {
    this.isCreating.set(!this.isCreating());
    if (!this.isCreating()) {
      this.resetForm();
    }
  }

  resetForm(): void {
    this.newTransaction = {
      type: 'EXPENSE', amount: 0, description: '',
      accountId: undefined, sourceAccountId: undefined, destinationAccountId: undefined
    };
  }

  setType(type: TransactionType): void {
    this.newTransaction.type = type;
  }

  saveTransaction(): void {
    if (!this.isValid()) return;

    this.apiService.createTransaction(this.newTransaction as Transaction).subscribe({
      next: (created) => {
        // Unshift to put at top
        this.transactions.update(txns => [created, ...txns]);
        this.toggleCreateForm();

        // In a real app, we'd notify Accounts state to update balances.
      },
      error: (err) => console.error('Error creating transaction', err)
    });
  }

  isValid(): boolean {
    if (!this.newTransaction.type || !this.newTransaction.amount || this.newTransaction.amount <= 0) return false;

    if (this.newTransaction.type === 'TRANSFER') {
      return !!this.newTransaction.sourceAccountId &&
        !!this.newTransaction.destinationAccountId &&
        this.newTransaction.sourceAccountId !== this.newTransaction.destinationAccountId;
    }

    return !!this.newTransaction.accountId;
  }
}
