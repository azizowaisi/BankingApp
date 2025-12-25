import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AccountsComponent } from '../accounts/accounts.component';

@Component({
  selector: 'app-transfers',
  templateUrl: './transfers.component.html',
  styleUrls: ['./transfers.component.scss'],
  standalone: false
})
export class TransfersComponent implements OnInit {
  transferForm: FormGroup;
  accounts: any[] = [];
  loading = false;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {
    this.transferForm = this.fb.group({
      fromIban: ['', Validators.required],
      toIban: ['', Validators.required],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.http.get<any[]>('/api/accounts').subscribe({
      next: (data) => {
        this.accounts = data.filter(acc => acc.status === 'ACTIVE');
        if (this.accounts.length > 0) {
          this.transferForm.patchValue({ fromIban: this.accounts[0].iban });
        }
      },
      error: () => {
        this.snackBar.open('Failed to load accounts', 'Close', { duration: 3000 });
      }
    });
  }

  onSubmit(): void {
    if (this.transferForm.valid) {
      this.loading = true;
      this.http.post('/api/transfers', this.transferForm.value).subscribe({
        next: () => {
          this.snackBar.open('Transfer completed successfully', 'Close', { duration: 3000 });
          this.transferForm.reset();
          if (this.accounts.length > 0) {
            this.transferForm.patchValue({ fromIban: this.accounts[0].iban });
          }
          this.loading = false;
        },
        error: (error) => {
          this.loading = false;
          this.snackBar.open(error.error?.message || 'Transfer failed', 'Close', { duration: 5000 });
        }
      });
    }
  }
}

