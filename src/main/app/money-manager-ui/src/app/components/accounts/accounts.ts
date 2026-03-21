import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { Account, AccountType } from '../../models/account.model';

@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, DatePipe, FormsModule],
  templateUrl: './accounts.html',
  styleUrl: './accounts.css'
})
export class Accounts implements OnInit {
  accounts = signal<Account[]>([]);
  isLoading = signal<boolean>(true);

  // Minimal state for creating a new account inline
  isCreating = signal<boolean>(false);
  newAccount: Partial<Account> = {
    name: '',
    type: 'BANK',
    balance: 0,
    provider: ''
  };

  availableTypes = signal<string[]>(['BANK', 'CREDIT_CARD', 'DEBIT_CARD', 'E_WALLET', 'CASH', 'SAVINGS', 'INVESTMENT']);

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.isLoading.set(true);
    this.apiService.getAccounts().subscribe({
      next: (data) => {
        this.accounts.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error fetching accounts', err);
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
    this.newAccount = { name: '', type: 'BANK', balance: 0, provider: '' };
  }

  saveAccount(): void {
    if (!this.newAccount.name || !this.newAccount.type) return;

    this.apiService.createAccount(this.newAccount as Account).subscribe({
      next: (created) => {
        this.accounts.update(accs => [...accs, created]);
        this.toggleCreateForm();
      },
      error: (err) => console.error('Error creating account', err)
    });
  }
}
