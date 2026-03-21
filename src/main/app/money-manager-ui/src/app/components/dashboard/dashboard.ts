import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { Account } from '../../models/account.model';
import { Transaction } from '../../models/transaction.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, CurrencyPipe, DatePipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  totalBalance = signal<number>(0);
  recentTransactions = signal<Transaction[]>([]);
  accounts = signal<Account[]>([]);
  isLoading = signal<boolean>(true);

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading.set(true);

    // Load Accounts
    this.apiService.getAccounts().subscribe({
      next: (accs) => {
        this.accounts.set(accs);
        const total = accs.reduce((sum, acc) => sum + acc.balance, 0);
        this.totalBalance.set(total);
      },
      error: (err) => console.error('Error fetching accounts', err)
    });

    // Load Transactions
    this.apiService.getTransactions().subscribe({
      next: (txns) => {
        // Sort by date descending and take top 5
        const sorted = txns.sort((a, b) => {
          const dateA = a.dateTime ? new Date(a.dateTime).getTime() : 0;
          const dateB = b.dateTime ? new Date(b.dateTime).getTime() : 0;
          return dateB - dateA;
        });
        this.recentTransactions.set(sorted.slice(0, 5));
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error fetching transactions', err);
        this.isLoading.set(false);
      }
    });
  }
}
