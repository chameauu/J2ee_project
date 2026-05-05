import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PatientDashboardService } from '../../../core/services/patient-dashboard.service';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardData } from '../../../core/models';

@Component({
  selector: 'app-patient-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class PatientDashboardComponent implements OnInit {
  private readonly dashboardService = inject(PatientDashboardService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  
  // Signals for reactive state management
  protected readonly dashboardData = signal<DashboardData | null>(null);
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal('');

  ngOnInit(): void {
    // Check if user is authenticated
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    // Check if user is a patient
    if (this.authService.getCurrentUserRole() !== 'PATIENT') {
      this.errorMessage.set('Access denied. This page is only for patients.');
      return;
    }

    this.loadDashboardData();
  }

  /**
   * Load all dashboard data
   */
  loadDashboardData(): void {
    const patientId = this.authService.getCurrentUserId();
    
    if (!patientId) {
      this.errorMessage.set('Unable to identify patient. Please log in again.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.dashboardService.getDashboardData(patientId).subscribe({
      next: (data) => {
        this.dashboardData.set(data);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading dashboard data:', error);
        this.errorMessage.set('Failed to load dashboard data. Please try again.');
        this.isLoading.set(false);
      }
    });
  }

  /**
   * Logout
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  /**
   * Format date for display
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  /**
   * Format datetime for display
   */
  formatDateTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Get status badge class
   */
  getStatusClass(status: string): string {
    const statusMap: Record<string, string> = {
      'SCHEDULED': 'badge-info',
      'COMPLETED': 'badge-success',
      'CANCELLED': 'badge-error',
      'PENDING': 'badge-warning',
      'DISPENSED': 'badge-success'
    };
    return statusMap[status] || 'badge-neutral';
  }
}
