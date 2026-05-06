import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { PatientDashboardService } from '../../../core/services/patient-dashboard.service';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardData } from '../../../core/models';

@Component({
  selector: 'app-patient-dashboard',
  imports: [CommonModule, RouterLink],
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

    // Load dashboard data from backend
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
   * Load mock data for testing (TEMPORARY)
   */
  loadMockData(): void {
    this.isLoading.set(true);
    
    // Simulate loading delay
    setTimeout(() => {
      const mockData: DashboardData = {
        patient: {
          id: 1,
          firstName: 'Jane',
          lastName: 'Doe',
          email: 'jane.doe@email.com',
          phone: '+1-555-0102',
          dateOfBirth: '1990-01-15',
          gender: 'FEMALE',
          hospitalId: 1,
          hospitalName: 'City Medical Center'
        },
        upcomingAppointments: [
          {
            id: 1,
            patientId: 1,
            doctorId: 2,
            appointmentDateTime: '2026-05-12T10:30:00',
            durationMinutes: 30,
            status: 'SCHEDULED',
            type: 'CONSULTATION',
            reason: 'Regular checkup',
            notes: '',
            createdAt: '2026-05-05T10:00:00',
            updatedAt: '2026-05-05T10:00:00'
          },
          {
            id: 2,
            patientId: 1,
            doctorId: 3,
            appointmentDateTime: '2026-05-18T14:00:00',
            durationMinutes: 45,
            status: 'SCHEDULED',
            type: 'FOLLOW_UP',
            reason: 'Blood pressure monitoring',
            notes: '',
            createdAt: '2026-05-05T10:00:00',
            updatedAt: '2026-05-05T10:00:00'
          },
          {
            id: 3,
            patientId: 1,
            doctorId: 2,
            appointmentDateTime: '2026-05-22T09:00:00',
            durationMinutes: 30,
            status: 'SCHEDULED',
            type: 'CONSULTATION',
            reason: 'Cardiology consultation',
            notes: '',
            createdAt: '2026-05-05T10:00:00',
            updatedAt: '2026-05-05T10:00:00'
          }
        ],
        recentMedicalRecords: [
          {
            id: 1,
            patientId: 1,
            doctorId: 2,
            visitDate: '2026-05-02',
            chiefComplaint: 'Annual Check-up',
            diagnosis: 'All clear – mild hypertension noted',
            treatment: 'Lifestyle modifications advised',
            notes: 'Patient advised to reduce sodium intake',
            createdAt: '2026-05-02T10:00:00',
            updatedAt: '2026-05-02T10:00:00'
          },
          {
            id: 2,
            patientId: 1,
            doctorId: 3,
            visitDate: '2026-03-18',
            chiefComplaint: 'Follow-up Consultation',
            diagnosis: 'Respiratory infection resolved',
            treatment: 'Completed antibiotic course',
            notes: 'Patient responding well to treatment',
            createdAt: '2026-03-18T10:00:00',
            updatedAt: '2026-03-18T10:00:00'
          },
          {
            id: 3,
            patientId: 1,
            doctorId: 4,
            visitDate: '2026-02-05',
            chiefComplaint: 'Dermatology Consult',
            diagnosis: 'Mild eczema (atopic dermatitis)',
            treatment: 'Topical corticosteroid prescribed',
            notes: 'Hydrocortisone 1% cream twice daily',
            createdAt: '2026-02-05T10:00:00',
            updatedAt: '2026-02-05T10:00:00'
          }
        ],
        activePrescriptions: [
          {
            id: 1,
            patientId: 1,
            doctorId: 2,
            medicationName: 'Amoxicillin 500mg',
            dosage: '500mg',
            frequency: '3 times daily',
            durationDays: 7,
            instructions: 'Take with food',
            status: 'ACTIVE',
            prescribedDate: '2026-05-02',
            validUntil: '2026-05-15',
            createdAt: '2026-05-02T10:00:00',
            updatedAt: '2026-05-02T10:00:00'
          },
          {
            id: 2,
            patientId: 1,
            doctorId: 2,
            medicationName: 'Ibuprofen 400mg',
            dosage: '400mg',
            frequency: '2 times daily',
            durationDays: 5,
            instructions: 'Take after meals',
            status: 'DISPENSED',
            prescribedDate: '2026-05-02',
            validUntil: '2026-05-10',
            createdAt: '2026-05-02T10:00:00',
            updatedAt: '2026-05-02T10:00:00'
          }
        ],
        stats: {
          totalAppointments: 8,
          upcomingAppointments: 3,
          activePrescriptions: 2,
          totalMedicalRecords: 10
        }
      };

      this.dashboardData.set(mockData);
      this.isLoading.set(false);
    }, 1000);
  }

  /**
   * Logout and redirect to login page
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
      'DISPENSED': 'badge-success',
      'ACTIVE': 'status-active',
      'EXPIRED': 'status-expired'
    };
    return statusMap[status] || 'badge-neutral';
  }

  /**
   * Get badge class for appointment status
   */
  getBadgeClass(status: string): string {
    const classMap: Record<string, string> = {
      'SCHEDULED': 'badge-scheduled',
      'COMPLETED': 'badge-completed',
      'CANCELLED': 'badge-cancelled'
    };
    return classMap[status] || 'badge-scheduled';
  }

  /**
   * Get initials from first and last name
   */
  getInitials(firstName: string, lastName: string): string {
    return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
  }

  /**
   * Get current date formatted
   */
  getCurrentDate(): string {
    const now = new Date();
    return now.toLocaleDateString('en-GB', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
  }

  /**
   * Get day from date string
   */
  getDay(dateString: string): string {
    const date = new Date(dateString);
    return date.getDate().toString();
  }

  /**
   * Get month from date string
   */
  getMonth(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short' });
  }

  /**
   * Format time from datetime string
   */
  formatTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Format date in short format
   */
  formatDateShort(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    });
  }
}
