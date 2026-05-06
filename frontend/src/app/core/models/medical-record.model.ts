export interface MedicalRecord {
  id: number;
  patientId: number;
  doctorId: number;
  doctorName?: string; // Optional: Doctor's full name
  doctorSpecialization?: string; // Optional: Doctor's specialization
  visitDate: string; // ISO datetime
  chiefComplaint: string;
  diagnosis: string;
  treatment: string;
  notes?: string;
  vitalSigns?: string;
  createdAt: string;
  updatedAt: string;
}
