# Hospital Management System - Angular Frontend Setup Plan

**Date**: May 5, 2026  
**Framework**: Angular 18+  
**Design System**: Custom Healthcare UI Components  
**Backend API**: Spring Boot REST API (http://localhost:8080)

---

## 🎯 Project Overview

Building a comprehensive Angular frontend for the Hospital Management System with:
- **Role-based dashboards** (Admin, Doctor, Patient, Pharmacist, Director)
- **JWT authentication** with HTTP interceptors
- **Responsive design** (mobile-first approach)
- **WCAG AA accessibility** compliance
- **Design system** with reusable components

---

## 📋 Phase 1: Project Setup & Foundation

### 1.1 Create Angular Project
```bash
# Create new Angular project with routing and SCSS
ng new hospital-management-frontend --routing --style=scss --strict

# Navigate to project
cd hospital-management-frontend

# Install dependencies
npm install
```

### 1.2 Install Required Dependencies
```bash
# Angular Material for UI components
ng add @angular/material

# HTTP client (already included in Angular)
# RxJS for reactive programming (already included)

# JWT handling
npm install @auth0/angular-jwt

# Chart library for dashboards
npm install chart.js ng2-charts

# Date handling
npm install date-fns

# Icons
npm install @angular/material-icons
```

### 1.3 Project Structure
```
src/
├── app/
│   ├── core/                    # Singleton services, guards, interceptors
│   │   ├── guards/
│   │   │   ├── auth.guard.ts
│   │   │   └── role.guard.ts
│   │   ├── interceptors/
│   │   │   ├── auth.interceptor.ts
│   │   │   └── error.interceptor.ts
│   │   ├── services/
│   │   │   ├── auth.service.ts
│   │   │   ├── api.service.ts
│   │   │   └── storage.service.ts
│   │   └── models/
│   │       ├── user.model.ts
│   │       ├── auth.model.ts
│   │       └── api-response.model.ts
│   │
│   ├── shared/                  # Shared components, directives, pipes
│   │   ├── components/
│   │   │   ├── header/
│   │   │   ├── sidebar/
│   │   │   ├── footer/
│   │   │   ├── loading-spinner/
│   │   │   └── error-message/
│   │   ├── directives/
│   │   └── pipes/
│   │
│   ├── features/                # Feature modules
│   │   ├── auth/
│   │   │   ├── login/
│   │   │   ├── register/
│   │   │   └── auth-routing.module.ts
│   │   │
│   │   ├── admin/
│   │   │   ├── dashboard/
│   │   │   ├── users/
│   │   │   ├── hospitals/
│   │   │   └── admin-routing.module.ts
│   │   │
│   │   ├── doctor/
│   │   │   ├── dashboard/
│   │   │   ├── patients/
│   │   │   ├── appointments/
│   │   │   ├── medical-records/
│   │   │   └── doctor-routing.module.ts
│   │   │
│   │   ├── patient/
│   │   │   ├── dashboard/
│   │   │   ├── appointments/
│   │   │   ├── medical-records/
│   │   │   ├── prescriptions/
│   │   │   └── patient-routing.module.ts
│   │   │
│   │   ├── pharmacist/
│   │   │   ├── dashboard/
│   │   │   ├── prescriptions/
│   │   │   ├── inventory/
│   │   │   └── pharmacist-routing.module.ts
│   │   │
│   │   └── director/
│   │       ├── dashboard/
│   │       ├── statistics/
│   │       ├── staff/
│   │       └── director-routing.module.ts
│   │
│   ├── design-system/           # Design system components
│   │   ├── tokens/
│   │   │   ├── colors.scss
│   │   │   ├── typography.scss
│   │   │   ├── spacing.scss
│   │   │   └── shadows.scss
│   │   ├── components/
│   │   │   ├── button/
│   │   │   ├── card/
│   │   │   ├── form-input/
│   │   │   ├── table/
│   │   │   └── modal/
│   │   └── design-system.module.ts
│   │
│   ├── app.component.ts
│   ├── app.component.html
│   ├── app.component.scss
│   ├── app-routing.module.ts
│   └── app.module.ts
│
├── assets/
│   ├── images/
│   ├── icons/
│   └── fonts/
│
├── styles/
│   ├── _variables.scss
│   ├── _mixins.scss
│   ├── _reset.scss
│   └── styles.scss
│
└── environments/
    ├── environment.ts
    └── environment.prod.ts
```

---

## 🎨 Phase 2: Design System Implementation

### 2.1 Design Tokens (SCSS Variables)
```scss
// styles/_variables.scss

// Color Tokens
$color-primary-100: #f0f9ff;
$color-primary-500: #3b82f6;
$color-primary-900: #1e3a8a;

$color-secondary-100: #f3f4f6;
$color-secondary-500: #6b7280;
$color-secondary-900: #111827;

$color-success: #10b981;
$color-warning: #f59e0b;
$color-error: #ef4444;
$color-info: #3b82f6;

// Healthcare-specific colors
$color-medical-blue: #0ea5e9;
$color-medical-green: #22c55e;
$color-medical-red: #dc2626;

// Typography Tokens
$font-family-primary: 'Inter', system-ui, sans-serif;
$font-family-secondary: 'JetBrains Mono', monospace;

$font-size-xs: 0.75rem;    // 12px
$font-size-sm: 0.875rem;   // 14px
$font-size-base: 1rem;     // 16px
$font-size-lg: 1.125rem;   // 18px
$font-size-xl: 1.25rem;    // 20px
$font-size-2xl: 1.5rem;    // 24px
$font-size-3xl: 1.875rem;  // 30px
$font-size-4xl: 2.25rem;   // 36px

// Spacing Tokens
$space-1: 0.25rem;   // 4px
$space-2: 0.5rem;    // 8px
$space-3: 0.75rem;   // 12px
$space-4: 1rem;      // 16px
$space-6: 1.5rem;    // 24px
$space-8: 2rem;      // 32px
$space-12: 3rem;     // 48px
$space-16: 4rem;     // 64px

// Shadow Tokens
$shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
$shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1);
$shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1);

// Transition Tokens
$transition-fast: 150ms ease;
$transition-normal: 300ms ease;
$transition-slow: 500ms ease;

// Breakpoints
$breakpoint-sm: 640px;
$breakpoint-md: 768px;
$breakpoint-lg: 1024px;
$breakpoint-xl: 1280px;
```

### 2.2 Base Component Library
- **Button Component**: Primary, secondary, tertiary variants
- **Card Component**: Container for content sections
- **Form Input Component**: Text, email, password, date inputs
- **Table Component**: Data display with sorting and pagination
- **Modal Component**: Dialogs and confirmations
- **Alert Component**: Success, warning, error, info messages
- **Loading Spinner**: Loading states
- **Badge Component**: Status indicators

---

## 🔐 Phase 3: Authentication & Authorization

### 3.1 Authentication Service
```typescript
// core/services/auth.service.ts
export class AuthService {
  login(email: string, password: string): Observable<AuthResponse>
  logout(): void
  getToken(): string | null
  getUserRole(): UserRole | null
  isAuthenticated(): boolean
  refreshToken(): Observable<AuthResponse>
}
```

### 3.2 HTTP Interceptor
```typescript
// core/interceptors/auth.interceptor.ts
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Add JWT token to all requests
    // Handle token refresh on 401 errors
  }
}
```

### 3.3 Route Guards
```typescript
// core/guards/auth.guard.ts
export class AuthGuard implements CanActivate {
  canActivate(): boolean {
    // Check if user is authenticated
    // Redirect to login if not
  }
}

// core/guards/role.guard.ts
export class RoleGuard implements CanActivate {
  canActivate(route: ActivatedRouteSnapshot): boolean {
    // Check if user has required role
    // Redirect to unauthorized page if not
  }
}
```

---

## 📱 Phase 4: Feature Modules

### 4.1 Admin Dashboard
- **Statistics Overview**: Total users, hospitals, appointments
- **User Management**: CRUD operations for all user types
- **Hospital Management**: CRUD operations for hospitals
- **System Settings**: Configuration and preferences

### 4.2 Doctor Dashboard
- **Today's Appointments**: List of scheduled appointments
- **Patient List**: Patients with medical records
- **Medical Records**: Create and view medical records
- **Profile Management**: Update own profile

### 4.3 Patient Dashboard
- **Upcoming Appointments**: Scheduled appointments
- **Medical History**: View medical records
- **Prescriptions**: Active and past prescriptions
- **Profile Management**: Update own profile

### 4.4 Pharmacist Dashboard
- **Pending Prescriptions**: Prescriptions to dispense
- **Inventory Management**: Medication stock levels
- **Dispensing History**: Past dispensed prescriptions
- **Low Stock Alerts**: Medications below reorder level

### 4.5 Director Dashboard
- **Hospital Statistics**: Doctors, patients, appointments count
- **Staff Overview**: Doctors and staff in hospital
- **Appointment Trends**: Charts and analytics
- **Profile Management**: Update own profile

---

## 🔌 Phase 5: API Integration

### 5.1 API Service Structure
```typescript
// core/services/api.service.ts
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';
  
  // Generic HTTP methods
  get<T>(endpoint: string): Observable<T>
  post<T>(endpoint: string, data: any): Observable<T>
  put<T>(endpoint: string, data: any): Observable<T>
  delete<T>(endpoint: string): Observable<T>
}
```

### 5.2 Feature-Specific Services
- **UserService**: User CRUD operations
- **DoctorService**: Doctor-specific operations
- **PatientService**: Patient-specific operations
- **AppointmentService**: Appointment management
- **MedicalRecordService**: Medical record operations
- **PrescriptionService**: Prescription management
- **HospitalService**: Hospital operations
- **StatisticsService**: Dashboard statistics

---

## ♿ Phase 6: Accessibility Implementation

### 6.1 WCAG AA Compliance
- **Color Contrast**: 4.5:1 ratio for normal text
- **Keyboard Navigation**: Full functionality without mouse
- **Screen Reader Support**: Semantic HTML and ARIA labels
- **Focus Management**: Clear focus indicators

### 6.2 Accessibility Features
- **Skip Navigation Links**: Jump to main content
- **Form Labels**: All inputs have associated labels
- **Error Messages**: Clear and descriptive
- **Alt Text**: All images have descriptive alt text
- **Heading Hierarchy**: Proper H1-H6 structure

---

## 📊 Phase 7: Testing Strategy

### 7.1 Unit Tests
- Component tests with Jasmine/Karma
- Service tests for business logic
- Guard and interceptor tests

### 7.2 Integration Tests
- API integration tests
- Authentication flow tests
- Role-based access tests

### 7.3 E2E Tests
- User journey tests with Protractor/Cypress
- Critical path testing
- Cross-browser testing

---

## 🚀 Phase 8: Deployment

### 8.1 Build Configuration
```bash
# Development build
ng build

# Production build
ng build --configuration production

# Build with optimization
ng build --prod --aot --build-optimizer
```

### 8.2 Environment Configuration
```typescript
// environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://api.hospital.com',
  jwtTokenKey: 'hospital_jwt_token'
};
```

---

## 📝 Implementation Timeline

### Week 1: Foundation
- ✅ Project setup and dependencies
- ✅ Design system implementation
- ✅ Base component library

### Week 2: Authentication
- ✅ Login/Register pages
- ✅ JWT authentication
- ✅ Route guards and interceptors

### Week 3: Core Features
- ✅ Admin dashboard
- ✅ Doctor dashboard
- ✅ Patient dashboard

### Week 4: Additional Features
- ✅ Pharmacist dashboard
- ✅ Director dashboard
- ✅ API integration

### Week 5: Polish & Testing
- ✅ Accessibility compliance
- ✅ Unit and integration tests
- ✅ Bug fixes and optimization

---

## 🎯 Success Criteria

- ✅ All role-based dashboards functional
- ✅ JWT authentication working correctly
- ✅ WCAG AA accessibility compliance
- ✅ Responsive design (mobile, tablet, desktop)
- ✅ 90%+ test coverage
- ✅ API integration complete
- ✅ Production-ready build

---

**Next Steps**: Start with Phase 1 - Project Setup & Foundation
