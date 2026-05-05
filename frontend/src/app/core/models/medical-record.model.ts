export interface MedicalRecord {
  id: number;
  patientId: number;
  doctorId: number;
  visitDate: string; // ISO datetime
  chiefComplaint: string;
  diagnosis: string;
  treatment: string;
  notes?: string;
  vitalSigns?: string;
  createdAt: string;
  updatedAt: string;
}
