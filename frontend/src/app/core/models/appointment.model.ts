export type AppointmentStatus = 'SCHEDULED' | 'COMPLETED' | 'CANCELLED';
export type AppointmentType = 'CONSULTATION' | 'FOLLOW_UP' | 'EMERGENCY' | 'ROUTINE_CHECKUP';

export interface Appointment {
  id: number;
  patientId: number;
  doctorId: number;
  appointmentDateTime: string; // ISO datetime: "2026-05-10T14:30:00"
  durationMinutes: number;
  status: AppointmentStatus;
  type: AppointmentType;
  reason?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}
