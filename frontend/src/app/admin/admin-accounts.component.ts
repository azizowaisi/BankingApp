import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';

export interface Account {
  id: string;
  iban: string;
  balance: number;
  status: string;
  userId: string;
  userName: string;
}

@Component({
  selector: 'app-admin-accounts',
  templateUrl: './admin-accounts.component.html',
  styleUrls: ['./admin-accounts.component.scss'],
  standalone: false
})
export class AdminAccountsComponent implements OnInit {
  accounts: Account[] = [];
  displayedColumns: string[] = ['iban', 'userName', 'balance', 'status', 'actions'];
  loading = false;

  constructor(
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.loading = true;
    this.http.get<Account[]>('/api/admin/accounts').subscribe({
      next: (data) => {
        this.accounts = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load accounts', 'Close', { duration: 3000 });
      }
    });
  }

  updateStatus(accountId: string, status: string): void {
    this.http.put(`/api/accounts/${accountId}/status?status=${status}`, {}).subscribe({
      next: () => {
        this.snackBar.open('Account status updated', 'Close', { duration: 3000 });
        this.loadAccounts();
      },
      error: () => {
        this.snackBar.open('Failed to update account status', 'Close', { duration: 3000 });
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'primary';
      case 'FROZEN': return 'warn';
      case 'CLOSED': return '';
      default: return '';
    }
  }
}

