import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { PatientDashboardService } from '../../../core/services/patient-dashboard.service';
import { MedicalRecord } from '../../../core/models';

interface RecordsByYear {
  [year: string]: MedicalRecord[];
}

@Component({
  selector: 'app-medical-records',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './medical-records.component.html',
  styleUrl: './medical-records.component.scss'
})
export class MedicalRecordsComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly dashboardService = inject(PatientDashboardService);
  private readonly router = inject(Router);

  // State
  protected readonly records = signal<MedicalRecord[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly selectedRecordId = signal<number | null>(null);

  // Filters
  protected readonly searchQuery = signal('');
  protected readonly filterYear = signal('');

  // Computed values
  protected readonly filteredRecords = computed(() => {
    let filtered = this.records();
    const query = this.searchQuery().toLowerCase();
    const year = this.filterYear();

    if (query) {
      filtered = filtered.filter(r =>
        r.chiefComplaint.toLowerCase().includes(query) ||
        r.diagnosis.toLowerCase().includes(query) ||
        r.treatment.toLowerCase().includes(query)
      );
    }

    if (year) {
      filtered = filtered.filter(r => {
        const recordYear = new Date(r.visitDate).getFullYear().toString();
        return recordYear === year;
      });
    }

    return filtered;
  });

  protected readonly recordsByYear = computed(() => {
    const grouped: RecordsByYear = {};
    this.filteredRecords().forEach(record => {
      const year = new Date(record.visitDate).getFullYear().toString();
      if (!grouped[year]) {
        grouped[year] = [];
      }
      grouped[year].push(record);
    });
    return grouped;
  });

  protected readonly years = computed(() => {
    return Object.keys(this.recordsByYear()).sort((a, b) => parseInt(b) - parseInt(a));
  });

  protected readonly selectedRecord = computed(() => {
    const id = this.selectedRecordId();
    if (!id) return null;
    return this.records().find(r => r.id === id) || null;
  });

  protected readonly stats = computed(() => {
    const all = this.records();
    return {
      total: all.length,
      thisYear: all.filter(r => new Date(r.visitDate).getFullYear() === new Date().getFullYear()).length,
      lastMonth: all.filter(r => {
        const date = new Date(r.visitDate);
        const now = new Date();
        return date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear();
      }).length
    };
  });

  protected readonly availableYears = computed(() => {
    const years = new Set<string>();
    this.records().forEach(r => {
      years.add(new Date(r.visitDate).getFullYear().toString());
    });
    return Array.from(years).sort((a, b) => parseInt(b) - parseInt(a));
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

    this.loadMedicalRecords();
  }

  /**
   * Load medical records from backend
   */
  loadMedicalRecords(): void {
    const patientId = this.authService.getCurrentUserId();
    if (!patientId) return;

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.dashboardService.getMedicalRecords(patientId).subscribe({
      next: (data) => {
        this.records.set(data);
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
   * Select a record to view details
   */
  selectRecord(id: number): void {
    this.selectedRecordId.set(id);
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
   * Format date with time
   */
  formatDateTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
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
   * Download record (placeholder)
   */
  downloadRecord(record: MedicalRecord): void {
    alert(`Download PDF for record #${record.id} - Feature to be implemented`);
  }

  /**
   * Logout
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
