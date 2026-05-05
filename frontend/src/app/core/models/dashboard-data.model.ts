import { Patient } from './patient.model';
import { Appointment } from './appointment.model';
import { MedicalRecord } from './medical-record.model';
import { Prescription } from './prescription.model';

export interface DashboardStats {
  totalAppointments: number;
  upcomingAppointments: number;
  activePrescriptions: number;
  totalMedicalRecords: number;
}

export interface DashboardData {
  patient: Patient;
  upcomingAppointments: Appointment[];
  recentMedicalRecords: MedicalRecord[];
  activePrescriptions: Prescription[];
  stats: DashboardStats;
}
