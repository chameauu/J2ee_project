import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorDashboardService } from '../../../core/services/doctor-dashboard.service';
import { MedicalRecord } from '../../../core/models';

@Component({
  selector: 'app-doctor-medical-records',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './medical-records.component.html',
  styleUrl: './medical-records.component.scss'
})
export class DoctorMedicalRecordsComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly dashboardService = inject(DoctorDashboardService);
  private readonly router = inject(Router);

  // State
  protected readonly medicalRecords = signal<MedicalRecord[]>([]);
  protected readonly filteredRecords = signal<MedicalRecord[]>([]);
  protected readonly selectedRecord = signal<MedicalRecord | null>(null);
  protected readonly searchQuery = signal('');
  protected readonly selectedYear = signal<string>('all');
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal('');

  // Computed values
  protected readonly doctorName = computed(() => {
    const user = this.authService.currentUser();
    return user ? user.email.split('@')[0] : 'Doctor';
  });

  protected readonly availableYears = computed(() => {
    const years = new Set<string>();
    this.medicalRecords().forEach(record => {
      const year = new Date(record.visitDate).getFullYear().toString();
      years.add(year);
    });
    return Array.from(years).sort((a, b) => parseInt(b) - parseInt(a));
  });

  protected readonly recordsByYear = computed(() => {
    const records = this.filteredRecords();
    const grouped = new Map<string, MedicalRecord[]>();

    records.forEach(record => {
      const year = new Date(record.visitDate).getFullYear().toString();
      if (!grouped.has(year)) {
        grouped.set(year, []);
      }
      grouped.get(year)!.push(record);
    });

    // Sort records within each year by date (newest first)
    grouped.forEach((records, year) => {
      records.sort((a, b) => 
        new Date(b.visitDate).getTime() - new Date(a.visitDate).getTime()
      );
    });

    // Convert to array and sort by year (newest first)
    return Array.from(grouped.entries())
      .sort((a, b) => parseInt(b[0]) - parseInt(a[0]));
  });

  protected readonly stats = computed(() => {
    const all = this.medicalRecords();
    const thisYear = new Date().getFullYear();
    const thisMonth = new Date().getMonth();

    return {
      total: all.length,
      thisYear: all.filter(r => 
        new Date(r.visitDate).getFullYear() === thisYear
      ).length,
      thisMonth: all.filter(r => {
        const date = new Date(r.visitDate);
        return date.getFullYear() === thisYear && date.getMonth() === thisMonth;
      }).length
    };
  });

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadMedicalRecords();
  }

  /**
   * Load all medical records for the doctor
   */
  loadMedicalRecords(): void {
    const doctorId = this.authService.getCurrentUserId();
    if (!doctorId) return;

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.dashboardService.getDoctorMedicalRecords(doctorId).subscribe({
      next: (records) => {
        this.medicalRecords.set(records);
        this.filteredRecords.set(records);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading medical records:', error);
        this.errorMessage.set('Failed to load medical records. Please try again.');
        this.isLoading.set(false);
      }
    });
  }

  /**
   * Filter records by search query and year
   */
  applyFilters(): void {
    const query = this.searchQuery().toLowerCase().trim();
    const year = this.selectedYear();
    
    let filtered = this.medicalRecords();

    // Filter by year
    if (year !== 'all') {
      filtered = filtered.filter(record => 
        new Date(record.visitDate).getFullYear().toString() === year
      );
    }

    // Filter by search query
    if (query) {
      filtered = filtered.filter(record => {
        const patientName = record.patientName?.toLowerCase() || '';
        const diagnosis = record.diagnosis.toLowerCase();
        const complaint = record.chiefComplaint.toLowerCase();
        const treatment = record.treatment.toLowerCase();
        
        return patientName.includes(query) ||
               diagnosis.includes(query) ||
               complaint.includes(query) ||
               treatment.includes(query);
      });
    }

    this.filteredRecords.set(filtered);
  }

  /**
   * Handle search input
   */
  onSearch(): void {
    this.applyFilters();
  }

  /**
   * Handle year filter change
   */
  onYearChange(): void {
    this.applyFilters();
  }

  /**
   * Select a record to view details
   */
  selectRecord(record: MedicalRecord): void {
    this.selectedRecord.set(record);
  }

  /**
   * Close record details
   */
  closeDetails(): void {
    this.selectedRecord.set(null);
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
