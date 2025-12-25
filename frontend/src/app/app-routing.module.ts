import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AccountsComponent } from './accounts/accounts.component';
import { TransfersComponent } from './transfers/transfers.component';
import { TransactionsComponent } from './transactions/transactions.component';
import { AdminDashboardComponent } from './admin/admin-dashboard.component';
import { AdminAccountsComponent } from './admin/admin-accounts.component';
import { AdminAuditLogsComponent } from './admin/admin-audit-logs.component';
import { AdminFraudEventsComponent } from './admin/admin-fraud-events.component';
import { AuthGuard } from './core/guards/auth.guard';
import { AdminGuard } from './core/guards/admin.guard';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'accounts', component: AccountsComponent },
      { path: 'transfers', component: TransfersComponent },
      { path: 'transactions', component: TransactionsComponent },
      { path: '', redirectTo: 'accounts', pathMatch: 'full' }
    ]
  },
  {
    path: 'admin',
    component: AdminDashboardComponent,
    canActivate: [AuthGuard, AdminGuard],
    children: [
      { path: 'accounts', component: AdminAccountsComponent },
      { path: 'audit-logs', component: AdminAuditLogsComponent },
      { path: 'fraud-events', component: AdminFraudEventsComponent },
      { path: '', redirectTo: 'accounts', pathMatch: 'full' }
    ]
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

