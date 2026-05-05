import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  Patient,
  Appointment,
  MedicalRecord,
  Prescription,
  DashboardData,
  DashboardStats
} from '../models';

@Injectable({
  providedIn: 'root'
})
export class PatientDashboardService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api';

  /**
   * Get complete dashboard data for a patient
   */
  getDashboardData(patientId: number): Observable<DashboardData> {
    return forkJoin({
      patient: this.getPatient(patientId),
      appointments: this.getAppointments(patientId),
      medicalRecords: this.getMedicalRecords(patientId),
      prescriptions: this.getPrescriptions(patientId)
    }).pipe(
      map(data => this.transformToDashboardData(data))
    );
  }

  /**
   * Get patient profile
   */
  getPatient(id: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.apiUrl}/patients/${id}`);
  }

  /**
   * Update patient profile
   */
  updatePatient(id: number, patient: Patient): Observable<Patient> {
    return this.http.put<Patient>(`${this.apiUrl}/patients/${id}`, patient);
  }

  /**
   * Get all appointments for a patient
   */
  getAppointments(patientId: number): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.apiUrl}/appointments/patient/${patientId}`);
  }

  /**
   * Get all medical records for a patient
   */
  getMedicalRecords(patientId: number): Observable<MedicalRecord[]> {
    return this.http.get<MedicalRecord[]>(`${this.apiUrl}/patients/${patientId}/medical-records`);
  }

  /**
   * Get all prescriptions for a patient
   */
  getPrescriptions(patientId: number): Observable<Prescription[]> {
    return this.http.get<Prescription[]>(`${this.apiUrl}/prescriptions/patient/${patientId}`);
  }

  /**
   * Transform raw API data into dashboard data structure
   */
  private transformToDashboardData(data: {
    patient: Patient;
    appointments: Appointment[];
    medicalRecords: MedicalRecord[];
    prescriptions: Prescription[];
  }): DashboardData {
    const now = new Date();

    // Filter upcoming appointments (scheduled and in the future)
    const upcomingAppointments = data.appointments
      .filter(a => 
        a.status === 'SCHEDULED' && 
        new Date(a.appointmentDateTime) > now
      )
      .sort((a, b) => 
        new Date(a.appointmentDateTime).getTime() - new Date(b.appointmentDateTime).getTime()
      )
      .slice(0, 3); // Get next 3 appointments

    // Get recent medical records (last 3)
    const recentMedicalRecords = data.medicalRecords
      .sort((a, b) => 
        new Date(b.visitDate).getTime() - new Date(a.visitDate).getTime()
      )
      .slice(0, 3);

    // Filter active prescriptions (pending or dispensed, and not expired)
    const activePrescriptions = data.prescriptions
      .filter(p => 
        (p.status === 'PENDING' || p.status === 'DISPENSED') && 
        new Date(p.validUntil) >= now
      );

    // Calculate statistics
    const stats: DashboardStats = {
      totalAppointments: data.appointments.length,
      upcomingAppointments: upcomingAppointments.length,
      activePrescriptions: activePrescriptions.length,
      totalMedicalRecords: data.medicalRecords.length
    };

    return {
      patient: data.patient,
      upcomingAppointments,
      recentMedicalRecords,
      activePrescriptions,
      stats
    };
  }
}
