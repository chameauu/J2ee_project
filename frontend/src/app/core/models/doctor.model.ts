export interface Doctor {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  specialization: string;
  licenseNumber: string;
  yearsOfExperience: number;
  qualification?: string;
  hospitalId: number;
  hospitalName?: string;
}
