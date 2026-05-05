import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  id: number;
  email: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8080/api/auth';
  private readonly TOKEN_KEY = 'jwt_token';
  private readonly USER_KEY = 'current_user';

  // Signals for reactive state
  readonly isAuthenticated = signal(false);
  readonly currentUser = signal<LoginResponse | null>(null);

  constructor(private http: HttpClient) {
    // Check if user is already logged in
    this.loadStoredAuth();
  }

  /**
   * Login with email and password
   */
  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, { email, password })
      .pipe(
        tap(response => {
          this.storeAuth(response);
        })
      );
  }

  /**
   * Logout and clear stored data
   */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.isAuthenticated.set(false);
    this.currentUser.set(null);
  }

  /**
   * Get stored JWT token
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Get current user ID
   */
  getCurrentUserId(): number | null {
    const user = this.currentUser();
    return user ? user.id : null;
  }

  /**
   * Get current user role
   */
  getCurrentUserRole(): string | null {
    const user = this.currentUser();
    return user ? user.role : null;
  }

  /**
   * Store authentication data
   */
  private storeAuth(response: LoginResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(response));
    this.isAuthenticated.set(true);
    this.currentUser.set(response);
  }

  /**
   * Load stored authentication data on app init
   */
  private loadStoredAuth(): void {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const userJson = localStorage.getItem(this.USER_KEY);

    if (token && userJson) {
      try {
        const user = JSON.parse(userJson) as LoginResponse;
        this.isAuthenticated.set(true);
        this.currentUser.set(user);
      } catch (error) {
        // Invalid stored data, clear it
        this.logout();
      }
    }
  }
}
