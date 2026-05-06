import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'patient/dashboard',
    loadComponent: () => import('./features/patient/dashboard/dashboard.component').then(m => m.PatientDashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'patient/appointments',
    loadComponent: () => import('./features/patient/appointments/appointments.component').then(m => m.AppointmentsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'patient/medical-records',
    loadComponent: () => import('./features/patient/medical-records/medical-records.component').then(m => m.MedicalRecordsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'patient/prescriptions',
    loadComponent: () => import('./features/patient/prescriptions/prescriptions.component').then(m => m.PrescriptionsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'doctor/dashboard',
    loadComponent: () => import('./features/doctor/dashboard/dashboard.component').then(m => m.DoctorDashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'doctor/schedule',
    loadComponent: () => import('./features/doctor/schedule/schedule.component').then(m => m.DoctorScheduleComponent),
    canActivate: [authGuard]
  },
  {
    path: 'doctor/patients',
    loadComponent: () => import('./features/doctor/patients/patients.component').then(m => m.DoctorPatientsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'doctor/medical-records',
    loadComponent: () => import('./features/doctor/medical-records/medical-records.component').then(m => m.DoctorMedicalRecordsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'doctor/prescriptions',
    loadComponent: () => import('./features/doctor/prescriptions/prescriptions.component').then(m => m.DoctorPrescriptionsComponent),
    canActivate: [authGuard]
  }
];
