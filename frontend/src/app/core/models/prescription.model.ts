export type PrescriptionStatus = 'ACTIVE' | 'DISPENSED' | 'EXPIRED' | 'CANCELLED';

export interface Prescription {
  id: number;
  patientId: number;
  doctorId: number;
  doctorName?: string; // Optional: Doctor's full name
  doctorSpecialization?: string; // Optional: Doctor's specialization
  medicalRecordId?: number;
  prescribedDate: string; // ISO datetime
  validUntil: string; // ISO date
  status: PrescriptionStatus;
  medicationName: string;
  dosage: string;
  frequency: string;
  durationDays?: number;
  instructions?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}
