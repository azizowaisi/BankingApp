import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';

/**
 * Login request interface.
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * Login response interface containing JWT token and user details.
 */
export interface LoginResponse {
  token: string;
  username: string;
  role: string;
}

/**
 * Authentication service for managing user authentication state.
 * 
 * Responsibilities:
 * - User login and registration
 * - JWT token management (stored in localStorage)
 * - Current user state management (RxJS BehaviorSubject)
 * - Role-based access checks
 * 
 * @author Banking Platform Team
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  /** Base API URL for authentication endpoints */
  private apiUrl = '/api';
  
  /** LocalStorage key for JWT token */
  private tokenKey = 'banking_token';
  
  /** LocalStorage key for user data */
  private userKey = 'banking_user';
  
  /** Observable stream of current user state */
  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    // Load user from localStorage on service initialization
    this.loadUserFromStorage();
  }

  /**
   * Authenticates user and stores JWT token.
   * 
   * On successful login:
   * - Stores JWT token in localStorage
   * - Stores user data in localStorage
   * - Updates current user observable
   * 
   * @param credentials Login credentials (username, password)
   * @returns Observable of login response
   */
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/auth/login`, credentials)
      .pipe(
        tap(response => {
          // Store JWT token for authenticated requests
          localStorage.setItem(this.tokenKey, response.token);
          
          // Store user data for UI state
          localStorage.setItem(this.userKey, JSON.stringify({
            username: response.username,
            role: response.role
          }));
          
          // Update observable for components
          this.currentUserSubject.next({
            username: response.username,
            role: response.role
          });
        })
      );
  }

  /**
   * Registers a new user account.
   * 
   * @param userData User registration data (username, password, email, firstName, lastName)
   * @returns Observable of registration response
   */
  register(userData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/register`, userData);
  }

  /**
   * Logs out the current user.
   * 
   * Clears:
   * - JWT token from localStorage
   * - User data from localStorage
   * - Current user observable state
   * 
   * Redirects to login page.
   */
  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  /**
   * Retrieves JWT token from localStorage.
   * 
   * @returns JWT token string or null if not found
   */
  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  /**
   * Checks if user is authenticated.
   * 
   * @returns true if JWT token exists, false otherwise
   */
  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  /**
   * Checks if current user has ADMIN role.
   * 
   * @returns true if user is ADMIN, false otherwise
   */
  isAdmin(): boolean {
    const user = this.currentUserSubject.value;
    return user?.role === 'ADMIN';
  }

  /**
   * Retrieves current user data.
   * 
   * @returns Current user object or null
   */
  getCurrentUser(): any {
    return this.currentUserSubject.value;
  }

  /**
   * Loads user data from localStorage on service initialization.
   * 
   * Used to restore user state after page refresh.
   */
  private loadUserFromStorage(): void {
    const userStr = localStorage.getItem(this.userKey);
    if (userStr) {
      this.currentUserSubject.next(JSON.parse(userStr));
    }
  }
}

