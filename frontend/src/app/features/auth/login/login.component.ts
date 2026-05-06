import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  // Form fields
  email = signal('');
  password = signal('');

  // UI state
  isLoading = signal(false);
  errorMessage = signal('');

  /**
   * Handle login form submission
   */
  onLogin(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.authService.login(this.email(), this.password()).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        
        // Check for return URL
        const returnUrl = this.route.snapshot.queryParams['returnUrl'];
        
        if (returnUrl) {
          this.router.navigateByUrl(returnUrl);
          return;
        }

        // Redirect based on role
        switch (response.role) {
          case 'PATIENT':
            this.router.navigate(['/patient/dashboard']);
            break;
          case 'DOCTOR':
            this.router.navigate(['/doctor/dashboard']);
            break;
          case 'PHARMACIST':
            this.router.navigate(['/pharmacist/dashboard']);
            break;
          case 'ADMINISTRATOR':
          case 'ADMIN':
            this.router.navigate(['/admin/dashboard']);
            break;
          case 'HOSPITAL_DIRECTOR':
          case 'DIRECTOR':
            this.router.navigate(['/director/dashboard']);
            break;
          default:
            this.router.navigate(['/']);
        }
      },
      error: (error) => {
        this.isLoading.set(false);
        this.errorMessage.set(
          error.status === 401 
            ? 'Invalid username or password' 
            : 'An error occurred. Please try again.'
        );
      }
    });
  }

  /**
   * Quick login for testing (Patient)
   */
  quickLoginPatient(): void {
    this.email.set('jane.doe@email.com');
    this.password.set('patient123');
    this.onLogin();
  }

  /**
   * Quick login for testing (Doctor)
   */
  quickLoginDoctor(): void {
    this.email.set('john.smith@hospital.com');
    this.password.set('doctor123');
    this.onLogin();
  }

  /**
   * Quick login for testing (Director)
   */
  quickLoginDirector(): void {
    this.email.set('director@hospital.com');
    this.password.set('director123');
    this.onLogin();
  }
}
