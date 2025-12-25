import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../services/auth.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(
    private router: Router,
    private snackBar: MatSnackBar,
    private authService: AuthService
  ) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<any> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          this.authService.logout();
          this.router.navigate(['/login']);
          this.snackBar.open('Session expired. Please login again.', 'Close', { duration: 3000 });
        } else if (error.status === 403) {
          this.snackBar.open('Access denied', 'Close', { duration: 3000 });
        } else if (error.error?.message) {
          this.snackBar.open(error.error.message, 'Close', { duration: 5000 });
        } else {
          this.snackBar.open('An error occurred', 'Close', { duration: 3000 });
        }
        return throwError(() => error);
      })
    );
  }
}

