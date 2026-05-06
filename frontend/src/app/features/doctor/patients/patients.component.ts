import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { HttpClient } from '@angular/common/http';
import { Patient } from '../../../core/models';

@Component({
  selector: 'app-doctor-patients',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './patients.component.html',
  styleUrl: './patients.component.scss'
})
export class DoctorPatientsComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiUrl = 'http://localhost:8080/api';

  // State
  protected readonly patients = signal<Patient[]>([]);
  protected readonly filteredPatients = signal<Patient[]>([]);
  protected readonly selectedPatient = signal<Patient | null>(null);
  protected readonly searchQuery = signal('');
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal('');

  // Computed values
  protected readonly doctorName = computed(() => {
    const user = this.authService.currentUser();
    return user ? user.email.split('@')[0] : 'Doctor';
  });

  protected readonly stats = computed(() => {
    const allPatients = this.patients();
    return {
      total: allPatients.length,
      male: allPatients.filter(p => p.gender === 'MALE').length,
      female: allPatients.filter(p => p.gender === 'FEMALE').length
    };
  });

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadPatients();
  }

  /**
   * Load all patients for the doctor
   */
  loadPatients(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    // Get all patients (doctors can see patients in their hospital)
    this.http.get<Patient[]>(`${this.apiUrl}/patients`).subscribe({
      next: (patients) => {
        this.patients.set(patients);
        this.filteredPatients.set(patients);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading patients:', error);
        this.errorMessage.set('Failed to load patients. Please try again.');
        this.isLoading.set(false);
      }
    });
  }

  /**
   * Search patients
   */
  onSearch(): void {
    const query = this.searchQuery().toLowerCase().trim();
    
    if (!query) {
      this.filteredPatients.set(this.patients());
      return;
    }

    const filtered = this.patients().filter(patient => {
      const fullName = `${patient.firstName} ${patient.lastName}`.toLowerCase();
      const email = patient.email.toLowerCase();
      const phone = patient.phone.toLowerCase();
      
      return fullName.includes(query) || 
             email.includes(query) || 
             phone.includes(query) ||
             (patient.bloodType && patient.bloodType.toLowerCase().includes(query));
    });

    this.filteredPatients.set(filtered);
  }

  /**
   * Select a patient to view details
   */
  selectPatient(patient: Patient): void {
    this.selectedPatient.set(patient);
  }

  /**
   * Close patient details
   */
  closeDetails(): void {
    this.selectedPatient.set(null);
  }

  /**
   * Calculate age from date of birth
   */
  calculateAge(dateOfBirth: string): number {
    const today = new Date();
    const birthDate = new Date(dateOfBirth);
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    
    return age;
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
  getPatientInitials(patient: Patient): string {
    return `${patient.firstName[0]}${patient.lastName[0]}`.toUpperCase();
  }

  /**
   * Logout
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
