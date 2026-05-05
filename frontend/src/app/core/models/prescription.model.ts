export type PrescriptionStatus = 'PENDING' | 'DISPENSED' | 'CANCELLED';

export interface Prescription {
  id: number;
  patientId: number;
  doctorId: number;
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
