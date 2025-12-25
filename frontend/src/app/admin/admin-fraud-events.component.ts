import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';

export interface FraudEvent {
  id: string;
  userId: string;
  username: string;
  type: string;
  timestamp: string;
  description: string;
  amount: number;
  severity: string;
}

@Component({
  selector: 'app-admin-fraud-events',
  templateUrl: './admin-fraud-events.component.html',
  styleUrls: ['./admin-fraud-events.component.scss'],
  standalone: false
})
export class AdminFraudEventsComponent implements OnInit {
  fraudEvents: FraudEvent[] = [];
  displayedColumns: string[] = ['timestamp', 'username', 'type', 'severity', 'amount', 'description'];
  loading = false;

  constructor(
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadFraudEvents();
  }

  loadFraudEvents(): void {
    this.loading = true;
    this.http.get<FraudEvent[]>('/api/admin/fraud-events').subscribe({
      next: (data) => {
        this.fraudEvents = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load fraud events', 'Close', { duration: 3000 });
      }
    });
  }

  getSeverityColor(severity: string): string {
    switch (severity) {
      case 'CRITICAL': return 'warn';
      case 'HIGH': return 'warn';
      case 'MEDIUM': return 'accent';
      case 'LOW': return 'primary';
      default: return '';
    }
  }
}

