import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';
import { environment } from '../../../../environments/environment';

interface DoctorPerformance {
  doctorId: number;
  doctorName: string;
  specialization: string;
  totalPatients: number;
  totalAppointments: number;
  completedAppointments: number;
  cancelledAppointments: number;
  completionRate: number;
  totalPrescriptions: number;
  totalMedicalRecords: number;
  todaysAppointments: number;
  utilizationRate: number;
  color?: string; // Generated on frontend
}

interface DirectorDashboardData {
  hospitalId: number;
  hospitalName: string;
  totalDoctors: number;
  totalPatients: number;
  totalPharmacists: number;
  totalAppointments: number;
  totalMedicalRecords: number;
  totalPrescriptions: number;
  appointmentCompletionRate: number;
  doctorUtilizationRate: number;
  averageAppointmentsPerDoctor: number;
  averagePatientsPerDoctor: number;
  todaysAppointments: number;
  todaysCompletedAppointments: number;
  scheduledAppointments: number;
  completedAppointments: number;
  cancelledAppointments: number;
  activePrescriptions: number;
  dispensedPrescriptions: number;
}

@Component({
  selector: 'app-director-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DirectorDashboardComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);

  // State
  protected readonly dashboardData = signal<DirectorDashboardData | null>(null);
  protected readonly doctorPerformance = signal<DoctorPerformance[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal('');

  // Computed values
  protected readonly directorName = computed(() => {
    const user = this.authService.currentUser();
    return user ? user.email.split('@')[0] : 'Director';
  });

  protected readonly topPerformers = computed(() => {
    return [...this.doctorPerformance()]
      .sort((a, b) => b.completionRate - a.completionRate)
      .slice(0, 5);
  });

  protected readonly specializationStats = computed(() => {
    const data = this.dashboardData();
    const doctors = this.doctorPerformance();
    
    if (!data) return [];
    
    return doctors.map(d => ({
      specialization: d.specialization,
      appointments: d.totalAppointments,
      percentage: Math.round((d.totalAppointments / data.totalAppointments) * 100)
    }));
  });

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadDashboardData();
  }

  /**
   * Load dashboard data from API
   */
  loadDashboardData(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    // Load main dashboard data
    this.http.get<DirectorDashboardData>(`${environment.apiUrl}/director/dashboard`).subscribe({
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

    // Load doctor performance data
    this.http.get<DoctorPerformance[]>(`${environment.apiUrl}/director/doctors/performance`).subscribe({
      next: (doctors) => {
        // Add color to each doctor based on their ID
        const doctorsWithColor = doctors.map(d => ({
          ...d,
          color: this.generateDoctorColor(d.doctorId)
        }));
        this.doctorPerformance.set(doctorsWithColor);
      },
      error: (error) => {
        console.error('Error loading doctor performance:', error);
      }
    });
  }

  /**
   * Generate consistent color for doctor based on ID
   */
  generateDoctorColor(doctorId: number): string {
    const colors = [
      '#0288D1', '#00897B', '#7B1FA2', '#E65100', 
      '#AD1457', '#1565C0', '#00695C', '#6A1B9A'
    ];
    return colors[doctorId % colors.length];
  }

  /**
   * Get doctor initials
   */
  getDoctorInitials(name: string): string {
    return name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
  }

  /**
   * Get completion rate badge class
   */
  getRateBadgeClass(rate: number): string {
    if (rate >= 93) return 'bg-success';
    if (rate >= 88) return 'bg-warning';
    return 'bg-danger';
  }

  /**
   * Get medal emoji for ranking
   */
  getMedalEmoji(index: number): string {
    const medals = ['🥇', '🥈', '🥉', '4️⃣', '5️⃣'];
    return medals[index] || '';
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
   * Get initials from name
   */
  getInitials(name: string): string {
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  /**
   * Logout
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
