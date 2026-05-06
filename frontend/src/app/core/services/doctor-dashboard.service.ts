import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  Doctor,
  Appointment,
  MedicalRecord,
  Prescription,
  Patient
} from '../models';

export interface DoctorDashboardData {
  doctor: Doctor;
  appointments: Appointment[];
  patients: Patient[];
  recentMedicalRecords: MedicalRecord[];
  prescriptions: Prescription[];
}

@Injectable({
  providedIn: 'root'
})
export class DoctorDashboardService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api';

  /**
   * Get complete dashboard data for a doctor
   */
  getDoctorDashboardData(doctorId: number): Observable<DoctorDashboardData> {
    return forkJoin({
      doctor: this.getDoctor(doctorId),
      appointments: this.getDoctorAppointments(doctorId),
      medicalRecords: this.getDoctorMedicalRecords(doctorId),
      prescriptions: this.getDoctorPrescriptions(doctorId)
    }).pipe(
      map(data => {
        // Extract unique patient IDs from appointments, medical records, and prescriptions
        const patientIds = new Set<number>();
        
        data.appointments.forEach(a => patientIds.add(a.patientId));
        data.medicalRecords.forEach(r => patientIds.add(r.patientId));
        data.prescriptions.forEach(p => patientIds.add(p.patientId));

        // For now, return empty patients array
        // Patients will need to be fetched separately or we'll use the patient info
        // already enriched in appointments/records/prescriptions
        const patients: Patient[] = [];

        return {
          doctor: data.doctor,
          appointments: data.appointments,
          patients: patients,
          recentMedicalRecords: data.medicalRecords.slice(0, 5),
          prescriptions: data.prescriptions
        };
      })
    );
  }

  /**
   * Get doctor profile
   */
  getDoctor(id: number): Observable<Doctor> {
    return this.http.get<Doctor>(`${this.apiUrl}/doctors/${id}`);
  }

  /**
   * Get all appointments for a doctor
   */
  getDoctorAppointments(doctorId: number): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.apiUrl}/appointments/doctor/${doctorId}`);
  }

  /**
   * Get all medical records created by a doctor
   */
  getDoctorMedicalRecords(doctorId: number): Observable<MedicalRecord[]> {
    return this.http.get<MedicalRecord[]>(`${this.apiUrl}/doctors/${doctorId}/medical-records`);
  }

  /**
   * Get all prescriptions written by a doctor
   */
  getDoctorPrescriptions(doctorId: number): Observable<Prescription[]> {
    return this.http.get<Prescription[]>(`${this.apiUrl}/prescriptions/doctor/${doctorId}`);
  }

  /**
   * Get patient by ID
   */
  getPatient(id: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.apiUrl}/patients/${id}`);
  }

  /**
   * Get all patients for a doctor
   */
  getDoctorPatients(doctorId: number): Observable<Patient[]> {
    return this.http.get<Patient[]>(`${this.apiUrl}/doctors/${doctorId}/patients`);
  }
}
