export interface Patient {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  dateOfBirth: string; // ISO date format: "1990-01-15"
  gender: 'MALE' | 'FEMALE';
  bloodType?: string;
  address?: string;
  emergencyContact?: string;
  insuranceNumber?: string;
  hospitalId: number;
  hospitalName?: string; // Read-only
}
