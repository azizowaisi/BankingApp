import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';

export interface AuditLog {
  id: string;
  action: string;
  userId: string;
  username: string;
  timestamp: string;
  details: string;
  ipAddress: string;
}

@Component({
  selector: 'app-admin-audit-logs',
  templateUrl: './admin-audit-logs.component.html',
  styleUrls: ['./admin-audit-logs.component.scss'],
  standalone: false
})
export class AdminAuditLogsComponent implements OnInit {
  auditLogs: AuditLog[] = [];
  displayedColumns: string[] = ['timestamp', 'action', 'username', 'details', 'ipAddress'];
  loading = false;

  constructor(
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadAuditLogs();
  }

  loadAuditLogs(): void {
    this.loading = true;
    this.http.get<AuditLog[]>('/api/admin/audit-logs').subscribe({
      next: (data) => {
        this.auditLogs = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load audit logs', 'Close', { duration: 3000 });
      }
    });
  }
}

