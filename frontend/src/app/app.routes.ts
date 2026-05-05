import { Routes } from '@angular/router';

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
    loadComponent: () => import('./features/patient/dashboard/dashboard.component').then(m => m.PatientDashboardComponent)
  }
];
