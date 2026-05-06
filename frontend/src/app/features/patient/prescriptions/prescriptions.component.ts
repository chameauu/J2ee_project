import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { PatientDashboardService } from '../../../core/services/patient-dashboard.service';
import { Prescription } from '../../../core/models';

type TabType = 'all' | 'ACTIVE' | 'DISPENSED';
type SortType = 'date-desc' | 'date-asc' | 'name';

@Component({
  selector: 'app-prescriptions',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './prescriptions.component.html',
  styleUrl: './prescriptions.component.scss'
})
export class PrescriptionsComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly dashboardService = inject(PatientDashboardService);
  private readonly router = inject(Router);

  // State
  protected readonly prescriptions = signal<Prescription[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly selectedPrescriptionId = signal<number | null>(null);

  // Filters
  protected readonly searchQuery = signal('');
  protected readonly activeTab = signal<TabType>('all');
  protected readonly sortBy = signal<SortType>('date-desc');

  // Computed values
  protected readonly filteredPrescriptions = computed(() => {
    let filtered = this.prescriptions();
    const query = this.searchQuery().toLowerCase();
    const tab = this.activeTab();

    // Filter by tab
    if (tab !== 'all') {
      filtered = filtered.filter(p => p.status === tab);
    }

    // Filter by search query
    if (query) {
      filtered = filtered.filter(p =>
        p.medicationName.toLowerCase().includes(query) ||
        p.instructions?.toLowerCase().includes(query)
      );
    }

    // Sort
    const sort = this.sortBy();
    if (sort === 'date-asc') {
      filtered = [...filtered].sort((a, b) => 
        new Date(a.prescribedDate).getTime() - new Date(b.prescribedDate).getTime()
      );
    } else if (sort === 'date-desc') {
      filtered = [...filtered].sort((a, b) => 
        new Date(b.prescribedDate).getTime() - new Date(a.prescribedDate).getTime()
      );
    } else if (sort === 'name') {
      filtered = [...filtered].sort((a, b) => 
        a.medicationName.localeCompare(b.medicationName)
      );
    }

    return filtered;
  });

  protected readonly selectedPrescription = computed(() => {
    const id = this.selectedPrescriptionId();
    if (!id) return null;
    return this.prescriptions().find(p => p.id === id) || null;
  });

  protected readonly stats = computed(() => {
    const all = this.prescriptions();
    const now = new Date();
    
    return {
      total: all.length,
      active: all.filter(p => p.status === 'ACTIVE' && new Date(p.validUntil) >= now).length,
      dispensed: all.filter(p => p.status === 'DISPENSED').length,
      expired: all.filter(p => new Date(p.validUntil) < now && p.status !== 'CANCELLED').length
    };
  });

  protected readonly patientName = computed(() => {
    const user = this.authService.currentUser();
    return user ? user.email.split('@')[0] : 'Patient';
  });

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadPrescriptions();
  }

  /**
   * Load prescriptions from backend
   */
  loadPrescriptions(): void {
    const patientId = this.authService.getCurrentUserId();
    if (!patientId) return;

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.dashboardService.getPrescriptions(patientId).subscribe({
      next: (data) => {
        this.prescriptions.set(data);
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
   * Select a prescription to view details
   */
  selectPrescription(id: number): void {
    this.selectedPrescriptionId.set(id);
  }

  /**
   * Set active tab
   */
  setTab(tab: TabType): void {
    this.activeTab.set(tab);
  }

  /**
   * Get status class
   */
  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      'ACTIVE': 'rx-active',
      'DISPENSED': 'rx-dispensed',
      'EXPIRED': 'rx-expired',
      'CANCELLED': 'rx-cancelled'
    };
    return map[status] || '';
  }

  /**
   * Get badge class
   */
  getBadgeClass(status: string): string {
    const map: Record<string, string> = {
      'ACTIVE': 'badge-active',
      'DISPENSED': 'badge-dispensed',
      'EXPIRED': 'badge-expired',
      'CANCELLED': 'badge-cancelled'
    };
    return map[status] || '';
  }

  /**
   * Check if prescription is expired
   */
  isExpired(prescription: Prescription): boolean {
    return new Date(prescription.validUntil) < new Date();
  }

  /**
   * Get days left until expiry
   */
  getDaysLeft(validUntil: string): number {
    const end = new Date(validUntil);
    const now = new Date();
    const diff = Math.ceil((end.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
    return diff;
  }

  /**
   * Get validity percentage
   */
  getValidityPercentage(prescription: Prescription): number {
    if (prescription.status !== 'ACTIVE') {
      return prescription.status === 'DISPENSED' ? 100 : 0;
    }

    const start = new Date(prescription.prescribedDate);
    const end = new Date(prescription.validUntil);
    const now = new Date();
    
    const total = end.getTime() - start.getTime();
    const elapsed = now.getTime() - start.getTime();
    const pct = Math.max(0, Math.min(100, 100 - (elapsed / total * 100)));
    
    return Math.round(pct);
  }

  /**
   * Get validity bar color class
   */
  getValidityColorClass(percentage: number): string {
    if (percentage > 50) return 'validity-ok';
    if (percentage > 20) return 'validity-warn';
    return 'validity-danger';
  }

  /**
   * Format date for display
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
   * Get doctor initials from prescription
   */
  getDoctorInitials(prescription: Prescription): string {
    if (prescription.doctorName) {
      // Extract initials from "Dr. FirstName LastName"
      const nameParts = prescription.doctorName.replace('Dr. ', '').split(' ');
      if (nameParts.length >= 2) {
        return `${nameParts[0][0]}${nameParts[1][0]}`.toUpperCase();
      }
      return nameParts[0].substring(0, 2).toUpperCase();
    }
    return 'DR';
  }

  /**
   * Download prescription (placeholder)
   */
  downloadPrescription(prescription: Prescription): void {
    alert(`Download PDF for prescription #${prescription.id} - Feature to be implemented`);
  }

  /**
   * Send to pharmacy (placeholder)
   */
  sendToPharmacy(prescription: Prescription): void {
    alert(`Send prescription #${prescription.id} to pharmacy - Feature to be implemented`);
  }

  /**
   * Logout
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
