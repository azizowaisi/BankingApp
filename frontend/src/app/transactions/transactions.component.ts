import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';

export interface Transaction {
  id: string;
  fromIban: string;
  toIban: string;
  amount: number;
  timestamp: string;
  status: string;
  description: string;
}

@Component({
  selector: 'app-transactions',
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.scss'],
  standalone: false
})
export class TransactionsComponent implements OnInit {
  transactions: Transaction[] = [];
  displayedColumns: string[] = ['timestamp', 'fromIban', 'toIban', 'amount', 'status', 'description'];
  accountId: string | null = null;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.accountId = params['id'];
      if (this.accountId) {
        this.loadTransactionHistory(this.accountId);
      } else {
        this.loadAllTransactions();
      }
    });
  }

  loadTransactionHistory(accountId: string): void {
    this.loading = true;
    this.http.get<Transaction[]>(`/api/transfers/history/${accountId}`).subscribe({
      next: (data) => {
        this.transactions = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load transactions', 'Close', { duration: 3000 });
      }
    });
  }

  loadAllTransactions(): void {
    this.loading = true;
    this.http.get<Transaction[]>('/api/accounts').subscribe({
      next: (accounts) => {
        if (accounts.length > 0) {
          this.loadTransactionHistory(accounts[0].id);
        } else {
          this.loading = false;
        }
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'primary';
      case 'FAILED': return 'warn';
      case 'REJECTED': return 'warn';
      default: return '';
    }
  }
}

