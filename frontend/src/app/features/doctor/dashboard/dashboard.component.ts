import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorDashboardService } from '../../../core/services/doctor-dashboard.service';
import { Appointment, MedicalRecord, Prescription, Patient } from '../../../core/models';
import { HttpClient } from '@angular/common/http';

interface DoctorStats {
  todayAppointments: number;
  upcomingAppointments: number;
  totalPatients: number;
  activePrescriptions: number;
}

@Component({
  selector: 'app-doctor-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DoctorDashboardComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly dashboardService = inject(DoctorDashboardService);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);

  // State
  protected readonly appointments = signal<Appointment[]>([]);
  protected readonly patients = signal<Patient[]>([]);
  protected readonly recentMedicalRecords = signal<MedicalRecord[]>([]);
  protected readonly prescriptions = signal<Prescription[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal('');

  // Modal state
  protected readonly showNewRecordModal = signal(false);
  protected readonly showNewPrescriptionModal = signal(false);
  protected readonly isSubmitting = signal(false);
  protected readonly submitError = signal('');

  // Form data
  protected newRecord = {
    patientId: 0,
    visitDate: '',
    chiefComplaint: '',
    diagnosis: '',
    treatment: '',
    notes: '',
    vitalSigns: ''
  };

  protected newPrescription = {
    patientId: 0,
    medicationName: '',
    dosage: '',
    frequency: '',
    durationDays: 0,
    instructions: '',
    notes: '',
    validUntil: ''
  };

  // Computed values
  protected readonly todayAppointments = computed(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);

    return this.appointments().filter(a => {
      const apptDate = new Date(a.appointmentDateTime);
      return apptDate >= today && apptDate < tomorrow && a.status === 'SCHEDULED';
    }).sort((a, b) => 
      new Date(a.appointmentDateTime).getTime() - new Date(b.appointmentDateTime).getTime()
    );
  });

  protected readonly upcomingAppointments = computed(() => {
    const now = new Date();
    return this.appointments().filter(a => 
      a.status === 'SCHEDULED' && new Date(a.appointmentDateTime) > now
    ).sort((a, b) => 
      new Date(a.appointmentDateTime).getTime() - new Date(b.appointmentDateTime).getTime()
    ).slice(0, 5);
  });

  protected readonly stats = computed((): DoctorStats => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);

    return {
      todayAppointments: this.appointments().filter(a => {
        const apptDate = new Date(a.appointmentDateTime);
        return apptDate >= today && apptDate < tomorrow && a.status === 'SCHEDULED';
      }).length,
      upcomingAppointments: this.appointments().filter(a => 
        a.status === 'SCHEDULED' && new Date(a.appointmentDateTime) > new Date()
      ).length,
      totalPatients: this.patients().length,
      activePrescriptions: this.prescriptions().filter(p => 
        p.status === 'ACTIVE' && new Date(p.validUntil) >= new Date()
      ).length
    };
  });

  protected readonly doctorName = computed(() => {
    const user = this.authService.currentUser();
    return user ? user.email.split('@')[0] : 'Doctor';
  });

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadDashboardData();
  }

  /**
   * Load all dashboard data
   */
  loadDashboardData(): void {
    const doctorId = this.authService.getCurrentUserId();
    if (!doctorId) return;

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.dashboardService.getDoctorDashboardData(doctorId).subscribe({
      next: (data) => {
        this.appointments.set(data.appointments);
        this.patients.set(data.patients);
        this.recentMedicalRecords.set(data.recentMedicalRecords);
        this.prescriptions.set(data.prescriptions);
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
   * Get appointment status badge class
   */
  getStatusBadgeClass(status: string): string {
    const map: Record<string, string> = {
      'SCHEDULED': 'bg-primary',
      'COMPLETED': 'bg-success',
      'CANCELLED': 'bg-danger'
    };
    return map[status] || 'bg-secondary';
  }

  /**
   * Get appointment type badge class
   */
  getTypeBadgeClass(type: string): string {
    const map: Record<string, string> = {
      'CONSULTATION': 'bg-info',
      'FOLLOW_UP': 'bg-warning',
      'EMERGENCY': 'bg-danger',
      'ROUTINE_CHECKUP': 'bg-success'
    };
    return map[type] || 'bg-secondary';
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
   * Format time for display
   */
  formatTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Format date and time for display
   */
  formatDateTime(dateString: string): string {
    return `${this.formatDate(dateString)} at ${this.formatTime(dateString)}`;
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
   * Get patient initials from name
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

  /**
   * Open new medical record modal
   */
  openNewRecordModal(): void {
    this.resetRecordForm();
    this.loadAllPatients(); // Load patients when opening modal
    this.showNewRecordModal.set(true);
  }

  /**
   * Close new medical record modal
   */
  closeNewRecordModal(): void {
    this.showNewRecordModal.set(false);
    this.submitError.set('');
  }

  /**
   * Reset medical record form
   */
  resetRecordForm(): void {
    const now = new Date();
    this.newRecord = {
      patientId: 0,
      visitDate: now.toISOString().slice(0, 16),
      chiefComplaint: '',
      diagnosis: '',
      treatment: '',
      notes: '',
      vitalSigns: ''
    };
  }

  /**
   * Load all patients (for dropdown)
   */
  loadAllPatients(): void {
    this.http.get<Patient[]>('/api/patients').subscribe({
      next: (patients) => {
        this.patients.set(patients);
      },
      error: (error) => {
        console.error('Error loading patients:', error);
        // If we can't load all patients, try to extract from existing data
        this.extractPatientsFromData();
      }
    });
  }

  /**
   * Extract patient info from appointments/records/prescriptions
   */
  extractPatientsFromData(): void {
    const patientMap = new Map<number, Patient>();

    // Extract from appointments
    this.appointments().forEach(appt => {
      if (appt.patientName && !patientMap.has(appt.patientId)) {
        const [firstName, ...lastNameParts] = appt.patientName.split(' ');
        patientMap.set(appt.patientId, {
          id: appt.patientId,
          firstName: firstName || '',
          lastName: lastNameParts.join(' ') || '',
          email: '',
          phone: '',
          dateOfBirth: '',
          gender: 'MALE',
          hospitalId: 1
        });
      }
    });

    this.patients.set(Array.from(patientMap.values()));
  }

  /**
   * Submit new medical record
   */
  submitNewRecord(): void {
    const doctorId = this.authService.getCurrentUserId();
    if (!doctorId) return;

    if (!this.newRecord.patientId || !this.newRecord.chiefComplaint || !this.newRecord.diagnosis || !this.newRecord.treatment) {
      this.submitError.set('Please fill in all required fields');
      return;
    }

    this.isSubmitting.set(true);
    this.submitError.set('');

    const payload = {
      patientId: this.newRecord.patientId,
      doctorId: doctorId,
      visitDate: this.newRecord.visitDate,
      chiefComplaint: this.newRecord.chiefComplaint,
      diagnosis: this.newRecord.diagnosis,
      treatment: this.newRecord.treatment,
      notes: this.newRecord.notes || null,
      vitalSigns: this.newRecord.vitalSigns || null
    };

    this.http.post<MedicalRecord>('/api/medical-records', payload).subscribe({
      next: (record) => {
        this.isSubmitting.set(false);
        this.closeNewRecordModal();
        this.loadDashboardData(); // Refresh data
      },
      error: (error) => {
        console.error('Error creating medical record:', error);
        this.submitError.set('Failed to create medical record. Please try again.');
        this.isSubmitting.set(false);
      }
    });
  }

  /**
   * Open new prescription modal
   */
  openNewPrescriptionModal(): void {
    this.resetPrescriptionForm();
    this.loadAllPatients(); // Load patients when opening modal
    this.showNewPrescriptionModal.set(true);
  }

  /**
   * Close new prescription modal
   */
  closeNewPrescriptionModal(): void {
    this.showNewPrescriptionModal.set(false);
    this.submitError.set('');
  }

  /**
   * Reset prescription form
   */
  resetPrescriptionForm(): void {
    const validUntil = new Date();
    validUntil.setDate(validUntil.getDate() + 30); // Default 30 days validity

    this.newPrescription = {
      patientId: 0,
      medicationName: '',
      dosage: '',
      frequency: '',
      durationDays: 30,
      instructions: '',
      notes: '',
      validUntil: validUntil.toISOString().slice(0, 10)
    };
  }

  /**
   * Submit new prescription
   */
  submitNewPrescription(): void {
    const doctorId = this.authService.getCurrentUserId();
    if (!doctorId) return;

    if (!this.newPrescription.patientId || !this.newPrescription.medicationName || 
        !this.newPrescription.dosage || !this.newPrescription.frequency) {
      this.submitError.set('Please fill in all required fields');
      return;
    }

    this.isSubmitting.set(true);
    this.submitError.set('');

    const payload = {
      patientId: this.newPrescription.patientId,
      doctorId: doctorId,
      medicationName: this.newPrescription.medicationName,
      dosage: this.newPrescription.dosage,
      frequency: this.newPrescription.frequency,
      durationDays: this.newPrescription.durationDays || null,
      instructions: this.newPrescription.instructions || null,
      notes: this.newPrescription.notes || null,
      validUntil: this.newPrescription.validUntil
    };

    this.http.post<Prescription>('/api/prescriptions', payload).subscribe({
      next: (prescription) => {
        this.isSubmitting.set(false);
        this.closeNewPrescriptionModal();
        this.loadDashboardData(); // Refresh data
      },
      error: (error) => {
        console.error('Error creating prescription:', error);
        this.submitError.set('Failed to create prescription. Please try again.');
        this.isSubmitting.set(false);
      }
    });
  }
}
