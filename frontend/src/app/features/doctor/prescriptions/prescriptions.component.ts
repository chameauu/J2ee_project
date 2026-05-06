import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorDashboardService } from '../../../core/services/doctor-dashboard.service';
import { Prescription, PrescriptionStatus } from '../../../core/models';

@Component({
  selector: 'app-doctor-prescriptions',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './prescriptions.component.html',
  styleUrl: './prescriptions.component.scss'
})
export class DoctorPrescriptionsComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly dashboardService = inject(DoctorDashboardService);
  private readonly router = inject(Router);

  // State
  protected readonly prescriptions = signal<Prescription[]>([]);
  protected readonly filteredPrescriptions = signal<Prescription[]>([]);
  protected readonly selectedPrescription = signal<Prescription | null>(null);
  protected readonly searchQuery = signal('');
  protected readonly selectedTab = signal<PrescriptionStatus | 'ALL'>('ALL');
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal('');

  // Computed values
  protected readonly doctorName = computed(() => {
    const user = this.authService.currentUser();
    return user ? user.email.split('@')[0] : 'Doctor';
  });

  protected readonly tabCounts = computed(() => {
    const all = this.prescriptions();
    return {
      all: all.length,
      active: all.filter(p => p.status === 'ACTIVE').length,
      dispensed: all.filter(p => p.status === 'DISPENSED').length,
      expired: all.filter(p => p.status === 'EXPIRED').length
    };
  });

  protected readonly stats = computed(() => {
    const all = this.prescriptions();
    const now = new Date();
    
    return {
      total: all.length,
      active: all.filter(p => p.status === 'ACTIVE').length,
      dispensed: all.filter(p => p.status === 'DISPENSED').length,
      expiringSoon: all.filter(p => {
        if (p.status !== 'ACTIVE') return false;
        const validUntil = new Date(p.validUntil);
        const daysUntilExpiry = Math.ceil((validUntil.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
        return daysUntilExpiry <= 7 && daysUntilExpiry > 0;
      }).length
    };
  });

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadPrescriptions();
  }

  /**
   * Load all prescriptions for the doctor
   */
  loadPrescriptions(): void {
    const doctorId = this.authService.getCurrentUserId();
    if (!doctorId) return;

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.dashboardService.getDoctorPrescriptions(doctorId).subscribe({
      next: (prescriptions) => {
        this.prescriptions.set(prescriptions);
        this.applyFilters();
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading prescriptions:', error);
        this.errorMessage.set('Failed to load prescriptions. Please try again.');
        this.isLoading.set(false);
      }
    });
  }

  /**
   * Apply filters based on tab and search query
   */
  applyFilters(): void {
    const query = this.searchQuery().toLowerCase().trim();
    const tab = this.selectedTab();
    
    let filtered = this.prescriptions();

    // Filter by tab
    if (tab !== 'ALL') {
      filtered = filtered.filter(p => p.status === tab);
    }

    // Filter by search query
    if (query) {
      filtered = filtered.filter(prescription => {
        const patientName = prescription.patientName?.toLowerCase() || '';
        const medication = prescription.medicationName.toLowerCase();
        const dosage = prescription.dosage.toLowerCase();
        
        return patientName.includes(query) ||
               medication.includes(query) ||
               dosage.includes(query);
      });
    }

    // Sort by prescribed date (newest first)
    filtered.sort((a, b) => 
      new Date(b.prescribedDate).getTime() - new Date(a.prescribedDate).getTime()
    );

    this.filteredPrescriptions.set(filtered);
  }

  /**
   * Handle search input
   */
  onSearch(): void {
    this.applyFilters();
  }

  /**
   * Change active tab
   */
  selectTab(tab: PrescriptionStatus | 'ALL'): void {
    this.selectedTab.set(tab);
    this.applyFilters();
  }

  /**
   * Select a prescription to view details
   */
  selectPrescription(prescription: Prescription): void {
    this.selectedPrescription.set(prescription);
  }

  /**
   * Close prescription details
   */
  closeDetails(): void {
    this.selectedPrescription.set(null);
  }

  /**
   * Calculate validity progress (0-100)
   */
  getValidityProgress(prescription: Prescription): number {
    const now = new Date();
    const prescribed = new Date(prescription.prescribedDate);
    const validUntil = new Date(prescription.validUntil);
    
    const totalDuration = validUntil.getTime() - prescribed.getTime();
    const elapsed = now.getTime() - prescribed.getTime();
    
    const progress = (elapsed / totalDuration) * 100;
    return Math.min(Math.max(progress, 0), 100);
  }

  /**
   * Get days until expiry
   */
  getDaysUntilExpiry(validUntil: string): number {
    const now = new Date();
    const expiry = new Date(validUntil);
    const diff = expiry.getTime() - now.getTime();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  }

  /**
   * Get status badge class
   */
  getStatusClass(status: PrescriptionStatus): string {
    const map: Record<PrescriptionStatus, string> = {
      'ACTIVE': 'badge-active',
      'DISPENSED': 'badge-dispensed',
      'EXPIRED': 'badge-expired',
      'CANCELLED': 'badge-cancelled'
    };
    return map[status] || 'badge-secondary';
  }

  /**
   * Format date
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }

  /**
   * Format date with time
   */
  formatDateTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
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
   * Get patient initials
   */
  getPatientInitials(patientName?: string): string {
    if (!patientName) return 'P';
    const parts = patientName.split(' ');
    if (parts.length >= 2) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
    }
    return patientName.substring(0, 2).toUpperCase();
  }

  /**
   * Logout
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
