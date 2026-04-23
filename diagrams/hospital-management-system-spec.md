# Hospital Management System - Multi-Tenant Architecture Specification

## Project Overview

A multi-tenant hospital management web application where multiple hospitals can use the same system with isolated data.

### Key Features by Role

**Doctor:**
- Consult patient files
- Add/update medical records
- View statistics (patients seen, appointments, etc.)
- View assigned appointments
- Write prescriptions

**Patient:**
- View medical history
- View appointments (read-only)
- Access prescriptions

**Pharmacy:**
- View patient prescriptions
- Check medication inventory
- Manage stock

**Administrator:**
- Visual dashboard with analytics
- Manage users (doctors, patients, staff)
- System configuration
- Reports and statistics

**Hospital Director:**
- Executive dashboard with comprehensive visualizations
- View all doctors and their performance metrics
- View all patients and demographics
- Monitor pharmacy inventory and stock levels
- Access system-wide analytics and reports
- Strategic decision-making insights

---

## Architecture Design

### Technology Stack

**Backend:**
- Spring Boot 3.x
- Spring Data JPA
- Spring Security (JWT authentication)
- MySQL/PostgreSQL (multi-tenant)
- Maven

**Frontend:**
- Angular/React
- Chart.js/D3.js (for dashboards)
- Bootstrap/Material UI

**Additional:**
- Redis (caching)
- Docker (containerization)
- Swagger (API documentation)

---

## Multi-Tenancy Strategy

### Approach: Separate Schema per Tenant

Each hospital gets its own database schema for complete data isolation.

```java
// Tenant Context Holder
public class TenantContext {
    private static ThreadLocal<String> currentTenant = new ThreadLocal<>();
    
    public static void setCurrentTenant(String tenant) {
        currentTenant.set(tenant);
    }
    
    public static String getCurrentTenant() {
        return currentTenant.get();
    }
    
    public static void clear() {
        currentTenant.remove();
    }
}

// Tenant Interceptor
@Component
public class TenantInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        String tenantId = request.getHeader("X-Tenant-ID");
        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId);
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler, 
                               Exception ex) {
        TenantContext.clear();
    }
}

// Dynamic DataSource Routing
public class TenantRoutingDataSource extends AbstractRoutingDataSource {
    
    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getCurrentTenant();
    }
}
```

---

## Database Design

### Core Entities

#### 1. User Management

```java
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String password;  // Hashed
    private String email;
    private String phone;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;  // DOCTOR, PATIENT, PHARMACIST, ADMIN, DIRECTOR
    
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}

@Entity
@Table(name = "doctors")
@Data
@EqualsAndHashCode(callSuper = true)
public class Doctor extends User {
    private String specialization;
    private String licenseNumber;
    private Integer yearsOfExperience;
    private String qualification;
    
    @OneToMany(mappedBy = "doctor")
    private List<Appointment> appointments;
    
    @OneToMany(mappedBy = "doctor")
    private List<MedicalNote> medicalNotes;
    
    @OneToMany(mappedBy = "doctor")
    private List<Prescription> prescriptions;
}

@Entity
@Table(name = "patients")
@Data
@EqualsAndHashCode(callSuper = true)
public class Patient extends User {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    private String bloodType;
    private String address;
    private String emergencyContact;
    private String insuranceNumber;
    
    @OneToMany(mappedBy = "patient")
    private List<MedicalRecord> medicalRecords;
    
    @OneToMany(mappedBy = "patient")
    private List<Appointment> appointments;
    
    @OneToMany(mappedBy = "patient")
    private List<Prescription> prescriptions;
}

@Entity
@Table(name = "pharmacists")
@Data
@EqualsAndHashCode(callSuper = true)
public class Pharmacist extends User {
    private String licenseNumber;
    private String pharmacyName;
    private String pharmacyAddress;
}

@Entity
@Table(name = "administrators")
@Data
@EqualsAndHashCode(callSuper = true)
public class Administrator extends User {
    private String department;
    private String accessLevel;
}

@Entity
@Table(name = "hospital_directors")
@Data
@EqualsAndHashCode(callSuper = true)
public class HospitalDirector extends User {
    private String hospitalName;
    private LocalDate appointmentDate;
    private String credentials;
}
```

#### 2. Medical Records

```java
@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;
    
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
    
    private LocalDateTime visitDate;
    private String chiefComplaint;
    private String diagnosis;
    private String treatment;
    
    @Column(columnDefinition = "TEXT")
    private String notes;  // Doctor's notes about the visit
    
    private String vitalSigns;  // JSON: {"bp": "120/80", "temp": "37", ...}
    
    @OneToMany(mappedBy = "medicalRecord")
    private List<Prescription> prescriptions;
}
```

#### 3. Appointments

```java
@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;
    
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
    
    private LocalDateTime appointmentDateTime;
    private Integer durationMinutes;
    
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;  // SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
    
    @Enumerated(EnumType.STRING)
    private AppointmentType type;  // CONSULTATION, FOLLOW_UP, EMERGENCY
    
    private String reason;
    private String notes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### 4. Prescriptions

```java
@Entity
@Table(name = "prescriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;
    
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
    
    @ManyToOne
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;
    
    private LocalDateTime prescribedDate;
    private LocalDate validUntil;
    
    @Enumerated(EnumType.STRING)
    private PrescriptionStatus status;  // ACTIVE, DISPENSED, EXPIRED, CANCELLED
    
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL)
    private List<PrescriptionItem> items;
    
    private String notes;
}

@Entity
@Table(name = "prescription_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;
    
    @ManyToOne
    @JoinColumn(name = "medication_id")
    private Medication medication;
    
    private String dosage;  // "500mg"
    private String frequency;  // "Twice daily"
    private Integer durationDays;
    private Integer quantity;
    private String instructions;
    
    private Boolean dispensed;
    private LocalDateTime dispensedAt;
    
    @ManyToOne
    @JoinColumn(name = "dispensed_by")
    private Pharmacist dispensedBy;
}
```

#### 6. Pharmacy & Medications

```java
@Entity
@Table(name = "medications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String genericName;
    private String manufacturer;
    
    @Enumerated(EnumType.STRING)
    private MedicationType type;  // TABLET, CAPSULE, SYRUP, INJECTION
    
    private String strength;
    private String description;
    
    @OneToMany(mappedBy = "medication")
    private List<PharmacyStock> stocks;
}

@Entity
@Table(name = "pharmacy_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "medication_id")
    private Medication medication;
    
    private Integer quantity;
    private Integer reorderLevel;
    private LocalDate expiryDate;
    private String batchNumber;
    private Double unitPrice;
    
    private LocalDateTime lastUpdated;
}
```

---

## Package Structure

```
src/main/java/com/hospital/management/
├── HospitalManagementApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── TenantConfig.java
│   ├── WebConfig.java
│   └── SwaggerConfig.java
├── multitenancy/
│   ├── TenantContext.java
│   ├── TenantInterceptor.java
│   ├── TenantRoutingDataSource.java
│   └── TenantIdentifierResolver.java
├── entities/
│   ├── user/
│   │   ├── User.java
│   │   ├── Doctor.java
│   │   ├── Patient.java
│   │   ├── Pharmacist.java
│   │   ├── Administrator.java
│   │   └── HospitalDirector.java
│   ├── medical/
│   │   ├── MedicalRecord.java
│   │   └── VitalSigns.java
│   ├── appointment/
│   │   └── Appointment.java
│   ├── prescription/
│   │   ├── Prescription.java
│   │   ├── PrescriptionItem.java
│   │   └── Medication.java
│   └── pharmacy/
│       └── PharmacyStock.java
├── repositories/
│   ├── user/
│   │   ├── UserRepository.java
│   │   ├── DoctorRepository.java
│   │   ├── PatientRepository.java
│   │   ├── PharmacistRepository.java
│   │   ├── AdministratorRepository.java
│   │   └── HospitalDirectorRepository.java
│   ├── medical/
│   │   └── MedicalRecordRepository.java
│   ├── appointment/
│   │   └── AppointmentRepository.java
│   ├── prescription/
│   │   ├── PrescriptionRepository.java
│   │   ├── PrescriptionItemRepository.java
│   │   └── MedicationRepository.java
│   └── pharmacy/
│       └── PharmacyStockRepository.java
├── services/
│   ├── user/
│   │   ├── IUserService.java
│   │   ├── UserServiceImpl.java
│   │   ├── IDoctorService.java
│   │   ├── DoctorServiceImpl.java
│   │   ├── IPatientService.java
│   │   └── PatientServiceImpl.java
│   ├── medical/
│   │   ├── IMedicalRecordService.java
│   │   ├── MedicalRecordServiceImpl.java
│   │   ├── IMedicalNoteService.java
│   │   └── MedicalNoteServiceImpl.java
│   ├── appointment/
│   │   ├── IAppointmentService.java
│   │   ├── AppointmentServiceImpl.java
│   │   ├── IScheduleService.java
│   │   └── ScheduleServiceImpl.java
│   ├── prescription/
│   │   ├── IPrescriptionService.java
│   │   └── PrescriptionServiceImpl.java
│   ├── pharmacy/
│   │   ├── IPharmacyService.java
│   │   └── PharmacyServiceImpl.java
│   ├── statistics/
│   │   ├── IStatisticsService.java
│   │   ├── StatisticsServiceImpl.java
│   │   ├── IDirectorService.java
│   │   └── DirectorServiceImpl.java
│   └── auth/
│       ├── IAuthService.java
│       └── AuthServiceImpl.java
├── controllers/
│   ├── DoctorController.java
│   ├── PatientController.java
│   ├── PharmacistController.java
│   ├── AdministratorController.java
│   ├── HospitalDirectorController.java
│   ├── AppointmentController.java
│   ├── PrescriptionController.java
│   ├── MedicalRecordController.java
│   ├── StatisticsController.java
│   └── AuthController.java
├── dto/
│   ├── user/
│   │   ├── UserDTO.java
│   │   ├── DoctorDTO.java
│   │   └── PatientDTO.java
│   ├── medical/
│   │   ├── MedicalRecordDTO.java
│   │   └── MedicalNoteDTO.java
│   ├── appointment/
│   │   └── AppointmentDTO.java
│   ├── prescription/
│   │   └── PrescriptionDTO.java
│   └── statistics/
│       └── DashboardStatsDTO.java
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── UserDetailsServiceImpl.java
├── exceptions/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   └── TenantNotFoundException.java
└── enums/
    ├── UserRole.java
    ├── Gender.java
    ├── AppointmentStatus.java
    ├── AppointmentType.java
    ├── PrescriptionStatus.java
    └── MedicationType.java
```

---

## API Endpoints Design

### Authentication
```
POST   /api/auth/login
POST   /api/auth/register
POST   /api/auth/refresh-token
POST   /api/auth/logout
GET    /api/auth/me
```

### Doctor Endpoints
```
GET    /api/doctors/patients                    # List all patients
GET    /api/doctors/patients/{id}               # Get patient details
GET    /api/doctors/patients/{id}/records       # Get patient medical records
POST   /api/doctors/patients/{id}/records       # Create medical record
GET    /api/doctors/patients/{id}/records/{recordId}  # Get specific record
PUT    /api/doctors/patients/{id}/records/{recordId}  # Update record

GET    /api/doctors/appointments                # Get doctor's appointments
GET    /api/doctors/appointments/today          # Today's appointments
GET    /api/doctors/appointments/upcoming       # Upcoming appointments
PUT    /api/doctors/appointments/{id}/status    # Update appointment status (complete, no-show)

POST   /api/doctors/prescriptions               # Write prescription
GET    /api/doctors/prescriptions/{id}          # Get prescription
PUT    /api/doctors/prescriptions/{id}          # Update prescription

GET    /api/doctors/statistics                  # Get doctor statistics
GET    /api/doctors/statistics/patients-count   # Total patients
GET    /api/doctors/statistics/appointments-today  # Today's appointments count
GET    /api/doctors/statistics/monthly-summary  # Monthly summary
```

### Patient Endpoints
```
GET    /api/patients/profile                    # Get patient profile
PUT    /api/patients/profile                    # Update profile

GET    /api/patients/medical-history            # Get medical history
GET    /api/patients/medical-history/{id}       # Get specific record

GET    /api/patients/appointments               # Get appointments (read-only)
GET    /api/patients/appointments/upcoming      # Upcoming appointments
GET    /api/patients/appointments/history       # Past appointments

GET    /api/patients/doctors                    # Get all available doctors
GET    /api/patients/doctors/{id}               # Get doctor details
GET    /api/patients/doctors/search?specialization=X  # Search doctors by specialization

GET    /api/patients/prescriptions              # Get prescriptions
GET    /api/patients/prescriptions/{id}         # Get specific prescription
GET    /api/patients/prescriptions/active       # Active prescriptions
```

### Pharmacist Endpoints
```
GET    /api/pharmacy/prescriptions              # Get all prescriptions
GET    /api/pharmacy/prescriptions/pending      # Pending prescriptions
GET    /api/pharmacy/prescriptions/{id}         # Get prescription details
PUT    /api/pharmacy/prescriptions/{id}/dispense  # Mark as dispensed

GET    /api/pharmacy/stock                      # Get medication stock
GET    /api/pharmacy/stock/low                  # Low stock items
POST   /api/pharmacy/stock                      # Add stock
PUT    /api/pharmacy/stock/{id}                 # Update stock
DELETE /api/pharmacy/stock/{id}                 # Remove stock

GET    /api/pharmacy/medications                # Get all medications
POST   /api/pharmacy/medications                # Add medication
PUT    /api/pharmacy/medications/{id}           # Update medication
```

### Administrator Endpoints
```
GET    /api/admin/dashboard                     # Dashboard statistics
GET    /api/admin/dashboard/overview            # System overview
GET    /api/admin/dashboard/charts              # Chart data

GET    /api/admin/users                         # Get all users
POST   /api/admin/users                         # Create user
GET    /api/admin/users/{id}                    # Get user
PUT    /api/admin/users/{id}                    # Update user
DELETE /api/admin/users/{id}                    # Delete user
PUT    /api/admin/users/{id}/activate           # Activate user
PUT    /api/admin/users/{id}/deactivate         # Deactivate user

GET    /api/admin/doctors                       # Get all doctors
GET    /api/admin/patients                      # Get all patients
GET    /api/admin/pharmacists                   # Get all pharmacists

GET    /api/admin/appointments                  # Get all appointments
POST   /api/admin/appointments                  # Create appointment for patient
PUT    /api/admin/appointments/{id}             # Update appointment
DELETE /api/admin/appointments/{id}             # Cancel appointment

GET    /api/admin/reports/appointments          # Appointments report
GET    /api/admin/reports/prescriptions         # Prescriptions report
GET    /api/admin/reports/revenue               # Revenue report
GET    /api/admin/reports/patients-demographics # Demographics report

GET    /api/admin/statistics/summary            # System summary
GET    /api/admin/statistics/trends             # Trends data
```

### Hospital Director Endpoints
```
GET    /api/director/dashboard                  # Executive dashboard
GET    /api/director/dashboard/overview         # High-level overview
GET    /api/director/dashboard/kpis             # Key Performance Indicators

GET    /api/director/doctors                    # View all doctors
GET    /api/director/doctors/{id}               # View doctor details
GET    /api/director/doctors/performance        # Doctor performance metrics
GET    /api/director/doctors/statistics         # Doctor statistics

GET    /api/director/patients                   # View all patients
GET    /api/director/patients/{id}              # View patient details
GET    /api/director/patients/demographics      # Patient demographics
GET    /api/director/patients/statistics        # Patient statistics

GET    /api/director/pharmacy/inventory         # View pharmacy inventory
GET    /api/director/pharmacy/stock-levels      # Stock levels overview
GET    /api/director/pharmacy/low-stock         # Low stock alerts
GET    /api/director/pharmacy/expiring          # Expiring medications

GET    /api/director/analytics/appointments     # Appointment analytics
GET    /api/director/analytics/prescriptions    # Prescription analytics
GET    /api/director/analytics/revenue          # Revenue analytics
GET    /api/director/analytics/trends           # Trend analysis

GET    /api/director/reports/executive-summary  # Executive summary report
GET    /api/director/reports/monthly            # Monthly report
GET    /api/director/reports/quarterly          # Quarterly report
GET    /api/director/reports/annual             # Annual report
```

---


## Key Service Implementations

### 1. Doctor Service

```java
public interface IDoctorService {
    // Patient Management
    List<PatientDTO> getAllPatients();
    PatientDTO getPatientById(Long patientId);
    Page<PatientDTO> searchPatients(String keyword, Pageable pageable);
    
    // Medical Records
    List<MedicalRecordDTO> getPatientRecords(Long patientId);
    MedicalRecordDTO createMedicalRecord(Long patientId, MedicalRecordDTO dto);
    MedicalRecordDTO updateMedicalRecord(Long recordId, MedicalRecordDTO dto);
    MedicalRecordDTO getMedicalRecord(Long recordId);
    
    // Appointments (View Only)
    List<AppointmentDTO> getDoctorAppointments(Long doctorId, LocalDate date);
    List<AppointmentDTO> getTodayAppointments(Long doctorId);
    List<AppointmentDTO> getUpcomingAppointments(Long doctorId);
    void updateAppointmentStatus(Long appointmentId, AppointmentStatus status);
    
    // Prescriptions
    PrescriptionDTO writePrescription(PrescriptionDTO dto);
    PrescriptionDTO updatePrescription(Long prescriptionId, PrescriptionDTO dto);
    PrescriptionDTO getPrescription(Long prescriptionId);
    
    // Statistics
    DoctorStatisticsDTO getStatistics(Long doctorId);
    Long getTotalPatientsCount(Long doctorId);
    Long getTodayAppointmentsCount(Long doctorId);
    Map<String, Object> getMonthlySummary(Long doctorId, int year, int month);
}

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements IDoctorService {
    
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalNoteRepository medicalNoteRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final PrescriptionRepository prescriptionRepository;
    
    @Override
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream()
            .map(PatientMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public MedicalRecordDTO createMedicalRecord(Long patientId, MedicalRecordDTO dto) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        
        MedicalRecord record = MedicalRecordMapper.toEntity(dto);
        record.setPatient(patient);
        record.setVisitDate(LocalDateTime.now());
        
        MedicalRecord saved = medicalRecordRepository.save(record);
        return MedicalRecordMapper.toDTO(saved);
    }
    
    @Override
    public DoctorStatisticsDTO getStatistics(Long doctorId) {
        DoctorStatisticsDTO stats = new DoctorStatisticsDTO();
        
        stats.setTotalPatients(patientRepository.countByDoctorId(doctorId));
        stats.setTodayAppointments(appointmentRepository.countByDoctorIdAndDate(
            doctorId, LocalDate.now()));
        stats.setTotalAppointments(appointmentRepository.countByDoctorId(doctorId));
        stats.setCompletedAppointments(appointmentRepository.countByDoctorIdAndStatus(
            doctorId, AppointmentStatus.COMPLETED));
        stats.setActivePrescriptions(prescriptionRepository.countByDoctorIdAndStatus(
            doctorId, PrescriptionStatus.ACTIVE));
        
        return stats;
    }
}
```

### 2. Patient Service

```java
public interface IPatientService {
    // Profile
    PatientDTO getProfile(Long patientId);
    PatientDTO updateProfile(Long patientId, PatientDTO dto);
    
    // Medical History
    List<MedicalRecordDTO> getMedicalHistory(Long patientId);
    MedicalRecordDTO getMedicalRecord(Long recordId);
    
    // Doctor Discovery
    List<DoctorDTO> getAllDoctors();
    DoctorDTO getDoctorById(Long doctorId);
    List<DoctorDTO> searchDoctorsBySpecialization(String specialization);
    
    // Appointments (Read-Only)
    List<AppointmentDTO> getAppointments(Long patientId);
    List<AppointmentDTO> getUpcomingAppointments(Long patientId);
    List<AppointmentDTO> getAppointmentHistory(Long patientId);
    
    // Prescriptions
    List<PrescriptionDTO> getPrescriptions(Long patientId);
    List<PrescriptionDTO> getActivePrescriptions(Long patientId);
    PrescriptionDTO getPrescription(Long prescriptionId);
}

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements IPatientService {
    
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalNoteRepository medicalNoteRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    
    @Override
    public List<MedicalRecordDTO> getMedicalHistory(Long patientId) {
        return medicalRecordRepository.findByPatientIdOrderByVisitDateDesc(patientId)
            .stream()
            .map(MedicalRecordMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public AppointmentDTO bookAppointment(AppointmentDTO dto) {
        // Check if slot is available
        boolean isAvailable = appointmentRepository.isSlotAvailable(
            dto.getDoctorId(), 
            dto.getAppointmentDateTime()
        );
        
        if (!isAvailable) {
            throw new SlotNotAvailableException("Time slot is not available");
        }
        
        Appointment appointment = AppointmentMapper.toEntity(dto);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setCreatedAt(LocalDateTime.now());
        
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentMapper.toDTO(saved);
    }
    
    @Override
    public List<PrescriptionDTO> getActivePrescriptions(Long patientId) {
        return prescriptionRepository.findByPatientIdAndStatus(
            patientId, 
            PrescriptionStatus.ACTIVE
        ).stream()
            .map(PrescriptionMapper::toDTO)
            .collect(Collectors.toList());
    }
}
```

### 3. Pharmacy Service

```java
public interface IPharmacyService {
    // Prescriptions
    List<PrescriptionDTO> getAllPrescriptions();
    List<PrescriptionDTO> getPendingPrescriptions();
    PrescriptionDTO getPrescription(Long prescriptionId);
    void dispensePrescription(Long prescriptionId, Long pharmacistId);
    
    // Stock Management
    List<PharmacyStockDTO> getAllStock();
    List<PharmacyStockDTO> getLowStockItems();
    PharmacyStockDTO addStock(PharmacyStockDTO dto);
    PharmacyStockDTO updateStock(Long stockId, PharmacyStockDTO dto);
    void removeStock(Long stockId);
    
    // Medications
    List<MedicationDTO> getAllMedications();
    MedicationDTO addMedication(MedicationDTO dto);
    MedicationDTO updateMedication(Long medicationId, MedicationDTO dto);
    void checkExpiringMedications();
}

@Service
@RequiredArgsConstructor
public class PharmacyServiceImpl implements IPharmacyService {
    
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final PharmacyStockRepository stockRepository;
    private final MedicationRepository medicationRepository;
    
    @Override
    @Transactional
    public void dispensePrescription(Long prescriptionId, Long pharmacistId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
            .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));
        
        // Check stock availability
        for (PrescriptionItem item : prescription.getItems()) {
            PharmacyStock stock = stockRepository.findByMedicationId(
                item.getMedication().getId()
            ).orElseThrow(() -> new OutOfStockException("Medication out of stock"));
            
            if (stock.getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                    "Insufficient stock for " + item.getMedication().getName()
                );
            }
        }
        
        // Dispense items
        for (PrescriptionItem item : prescription.getItems()) {
            PharmacyStock stock = stockRepository.findByMedicationId(
                item.getMedication().getId()
            ).get();
            
            // Reduce stock
            stock.setQuantity(stock.getQuantity() - item.getQuantity());
            stockRepository.save(stock);
            
            // Mark item as dispensed
            item.setDispensed(true);
            item.setDispensedAt(LocalDateTime.now());
            prescriptionItemRepository.save(item);
        }
        
        // Update prescription status
        prescription.setStatus(PrescriptionStatus.DISPENSED);
        prescriptionRepository.save(prescription);
    }
    
    @Override
    public List<PharmacyStockDTO> getLowStockItems() {
        return stockRepository.findLowStockItems()
            .stream()
            .map(PharmacyStockMapper::toDTO)
            .collect(Collectors.toList());
    }
}
```

### 4. Statistics Service

```java
public interface IStatisticsService {
    // Dashboard
    DashboardStatsDTO getDashboardStats();
    Map<String, Object> getSystemOverview();
    Map<String, Object> getChartData(String chartType);
    
    // Reports
    AppointmentReportDTO getAppointmentsReport(LocalDate startDate, LocalDate endDate);
    PrescriptionReportDTO getPrescriptionsReport(LocalDate startDate, LocalDate endDate);
    PatientDemographicsDTO getPatientDemographics();
    
    // Trends
    List<TrendDataDTO> getAppointmentTrends(int months);
    List<TrendDataDTO> getPatientRegistrationTrends(int months);
}

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements IStatisticsService {
    
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    
    @Override
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        stats.setTotalDoctors(userRepository.countByRole(UserRole.DOCTOR));
        stats.setTotalPatients(userRepository.countByRole(UserRole.PATIENT));
        stats.setTotalPharmacists(userRepository.countByRole(UserRole.PHARMACIST));
        
        stats.setTodayAppointments(appointmentRepository.countByDate(LocalDate.now()));
        stats.setCompletedAppointments(appointmentRepository.countByStatusAndDate(
            AppointmentStatus.COMPLETED, LocalDate.now()));
        stats.setPendingAppointments(appointmentRepository.countByStatusAndDate(
            AppointmentStatus.SCHEDULED, LocalDate.now()));
        
        stats.setActivePrescriptions(prescriptionRepository.countByStatus(
            PrescriptionStatus.ACTIVE));
        
        stats.setTotalMedicalRecords(medicalRecordRepository.count());
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getChartData(String chartType) {
        Map<String, Object> data = new HashMap<>();
        
        switch (chartType) {
            case "appointments-by-month":
                data.put("labels", getLastSixMonths());
                data.put("data", getAppointmentCountsByMonth());
                break;
                
            case "patients-by-age-group":
                data.put("labels", Arrays.asList("0-18", "19-35", "36-50", "51-65", "65+"));
                data.put("data", getPatientCountsByAgeGroup());
                break;
                
            case "prescriptions-by-status":
                data.put("labels", Arrays.asList("Active", "Dispensed", "Expired"));
                data.put("data", getPrescriptionCountsByStatus());
                break;
        }
        
        return data;
    }
    
    @Override
    public PatientDemographicsDTO getPatientDemographics() {
        PatientDemographicsDTO demographics = new PatientDemographicsDTO();
        
        demographics.setTotalPatients(userRepository.countByRole(UserRole.PATIENT));
        demographics.setMaleCount(userRepository.countByRoleAndGender(
            UserRole.PATIENT, Gender.MALE));
        demographics.setFemaleCount(userRepository.countByRoleAndGender(
            UserRole.PATIENT, Gender.FEMALE));
        
        demographics.setAgeDistribution(getPatientCountsByAgeGroup());
        demographics.setBloodTypeDistribution(getPatientCountsByBloodType());
        
        return demographics;
    }
}
```

---

## Security Implementation

### JWT Authentication

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .cors()
            .and()
            .authorizeHttpRequests()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/doctors/**").hasRole("DOCTOR")
                .requestMatchers("/api/patients/**").hasRole("PATIENT")
                .requestMatchers("/api/pharmacy/**").hasRole("PHARMACIST")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/director/**").hasRole("DIRECTOR")
                .anyRequest().authenticated()
            .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
            throws Exception {
        return config.getAuthenticationManager();
    }
}

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String token = getTokenFromRequest(request);
        
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
                );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

---

## Frontend Architecture (Angular/React)

### Component Structure

```
src/
├── app/
│   ├── core/
│   │   ├── services/
│   │   │   ├── auth.service.ts
│   │   │   ├── api.service.ts
│   │   │   └── tenant.service.ts
│   │   ├── guards/
│   │   │   ├── auth.guard.ts
│   │   │   └── role.guard.ts
│   │   ├── interceptors/
│   │   │   ├── auth.interceptor.ts
│   │   │   └── tenant.interceptor.ts
│   │   └── models/
│   │       ├── user.model.ts
│   │       ├── patient.model.ts
│   │       └── appointment.model.ts
│   ├── modules/
│   │   ├── auth/
│   │   │   ├── login/
│   │   │   ├── register/
│   │   │   └── auth.module.ts
│   │   ├── doctor/
│   │   │   ├── dashboard/
│   │   │   ├── patients/
│   │   │   ├── appointments/
│   │   │   ├── prescriptions/
│   │   │   ├── schedule/
│   │   │   └── doctor.module.ts
│   │   ├── patient/
│   │   │   ├── dashboard/
│   │   │   ├── medical-history/
│   │   │   ├── appointments/
│   │   │   ├── prescriptions/
│   │   │   └── patient.module.ts
│   │   ├── pharmacy/
│   │   │   ├── dashboard/
│   │   │   ├── prescriptions/
│   │   │   ├── stock/
│   │   │   └── pharmacy.module.ts
│   │   └── admin/
│   │       ├── dashboard/
│   │       ├── users/
│   │       ├── reports/
│   │       └── admin.module.ts
│   ├── shared/
│   │   ├── components/
│   │   │   ├── navbar/
│   │   │   ├── sidebar/
│   │   │   ├── table/
│   │   │   └── chart/
│   │   ├── pipes/
│   │   └── directives/
│   └── app.module.ts
```

### Key Frontend Services

```typescript
// auth.service.ts
@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject: BehaviorSubject<User>;
  public currentUser: Observable<User>;

  constructor(private http: HttpClient) {
    this.currentUserSubject = new BehaviorSubject<User>(
      JSON.parse(localStorage.getItem('currentUser'))
    );
    this.currentUser = this.currentUserSubject.asObservable();
  }

  login(username: string, password: string, tenantId: string) {
    return this.http.post<any>('/api/auth/login', { username, password })
      .pipe(map(response => {
        localStorage.setItem('currentUser', JSON.stringify(response.user));
        localStorage.setItem('token', response.token);
        localStorage.setItem('tenantId', tenantId);
        this.currentUserSubject.next(response.user);
        return response.user;
      }));
  }

  logout() {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
    localStorage.removeItem('tenantId');
    this.currentUserSubject.next(null);
  }

  get currentUserValue(): User {
    return this.currentUserSubject.value;
  }
}

// tenant.interceptor.ts
@Injectable()
export class TenantInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const tenantId = localStorage.getItem('tenantId');
    
    if (tenantId) {
      req = req.clone({
        setHeaders: {
          'X-Tenant-ID': tenantId
        }
      });
    }
    
    return next.handle(req);
  }
}
```

---

## Database Schema (MySQL)

```sql
-- Users table (base)
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    role ENUM('DOCTOR', 'PATIENT', 'PHARMACIST', 'ADMIN', 'DIRECTOR') NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- Doctors table
CREATE TABLE doctors (
    id BIGINT PRIMARY KEY,
    specialization VARCHAR(100),
    license_number VARCHAR(50) UNIQUE,
    years_of_experience INT,
    qualification VARCHAR(255),
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

-- Patients table
CREATE TABLE patients (
    id BIGINT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    blood_type VARCHAR(5),
    address TEXT,
    emergency_contact VARCHAR(100),
    insurance_number VARCHAR(50),
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_name (first_name, last_name),
    INDEX idx_dob (date_of_birth)
);

-- Medical Records table
CREATE TABLE medical_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    visit_date TIMESTAMP NOT NULL,
    chief_complaint TEXT,
    diagnosis TEXT,
    treatment TEXT,
    vital_signs JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    INDEX idx_patient (patient_id),
    INDEX idx_doctor (doctor_id),
    INDEX idx_visit_date (visit_date)
);

-- Medical Notes table
CREATE TABLE medical_notes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    medical_record_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    note_type ENUM('PROGRESS', 'CONSULTATION', 'FOLLOW_UP'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (medical_record_id) REFERENCES medical_records(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    INDEX idx_record (medical_record_id)
);

-- Appointments table
CREATE TABLE appointments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    appointment_date_time TIMESTAMP NOT NULL,
    duration_minutes INT DEFAULT 30,
    status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW') DEFAULT 'SCHEDULED',
    type ENUM('CONSULTATION', 'FOLLOW_UP', 'EMERGENCY'),
    reason VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    INDEX idx_patient (patient_id),
    INDEX idx_doctor (doctor_id),
    INDEX idx_date (appointment_date_time),
    INDEX idx_status (status)
);

-- Prescriptions table
CREATE TABLE prescriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    medical_record_id BIGINT,
    prescribed_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valid_until DATE,
    status ENUM('ACTIVE', 'DISPENSED', 'EXPIRED', 'CANCELLED') DEFAULT 'ACTIVE',
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (medical_record_id) REFERENCES medical_records(id),
    INDEX idx_patient (patient_id),
    INDEX idx_status (status)
);

-- Medications table
CREATE TABLE medications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    generic_name VARCHAR(255),
    manufacturer VARCHAR(255),
    type ENUM('TABLET', 'CAPSULE', 'SYRUP', 'INJECTION', 'CREAM', 'OTHER'),
    strength VARCHAR(50),
    description TEXT,
    INDEX idx_name (name)
);

-- Pharmacy Stock table
CREATE TABLE pharmacy_stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    medication_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    reorder_level INT DEFAULT 10,
    expiry_date DATE,
    batch_number VARCHAR(50),
    unit_price DECIMAL(10, 2),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (medication_id) REFERENCES medications(id),
    INDEX idx_medication (medication_id),
    INDEX idx_expiry (expiry_date)
);
```

---

## Deployment Strategy

### Docker Compose Setup

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: hospital_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/hospital_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root_password
      SPRING_REDIS_HOST: redis
    depends_on:
      - mysql
      - redis

  frontend:
    build: ./frontend
    ports:
      - "4200:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

---

## Development Roadmap

### Phase 1: Core Features (Weeks 1-4)
- [ ] Multi-tenancy setup
- [ ] User authentication & authorization
- [ ] Doctor module (patient files, notes)
- [ ] Patient module (history, appointments)
- [ ] Basic appointment system

### Phase 2: Medical Features (Weeks 5-8)
- [ ] Prescription management
- [ ] Pharmacy module
- [ ] Lab results integration
- [ ] Medical records enhancement
- [ ] Doctor schedule management

### Phase 3: Analytics & Admin (Weeks 9-12)
- [ ] Admin dashboard
- [ ] Statistics and reports
- [ ] User management
- [ ] System configuration
- [ ] Data export features

### Phase 4: Advanced Features (Weeks 13-16)
- [ ] Real-time notifications
- [ ] Email/SMS reminders
- [ ] Document upload (X-rays, reports)
- [ ] Telemedicine integration
- [ ] Mobile app (optional)

---

## Testing Strategy

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {
    @Mock
    private PatientRepository patientRepository;
    
    @InjectMocks
    private DoctorServiceImpl doctorService;
    
    @Test
    void testGetAllPatients() {
        // Test implementation
    }
}
```

### Integration Tests
```java
@SpringBootTest
@AutoConfigureMockMvc
class DoctorControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testGetPatients() throws Exception {
        mockMvc.perform(get("/api/doctors/patients"))
            .andExpect(status().isOk());
    }
}
```

---

## Conclusion

This hospital management system follows all Spring Boot best practices:
- ✅ Layered architecture
- ✅ Repository pattern
- ✅ Service layer with interfaces
- ✅ DTOs for API responses
- ✅ JWT authentication
- ✅ Multi-tenancy support
- ✅ RESTful API design
- ✅ Comprehensive error handling
- ✅ Pagination and sorting
- ✅ Transaction management

The system is scalable, maintainable, and ready for production deployment.


### 5. Hospital Director Service

```java
public interface IDirectorService {
    // Dashboard & KPIs
    DirectorDashboardDTO getExecutiveDashboard();
    Map<String, Object> getKPIs();
    Map<String, Object> getOverview();
    
    // Doctor Management (View Only)
    List<DoctorDTO> getAllDoctors();
    DoctorDTO getDoctorById(Long doctorId);
    DoctorPerformanceDTO getDoctorPerformance(Long doctorId);
    Map<String, Object> getDoctorStatistics();
    
    // Patient Management (View Only)
    List<PatientDTO> getAllPatients();
    PatientDTO getPatientById(Long patientId);
    PatientDemographicsDTO getPatientDemographics();
    Map<String, Object> getPatientStatistics();
    
    // Pharmacy Oversight
    List<PharmacyStockDTO> getPharmacyInventory();
    Map<String, Object> getStockLevels();
    List<PharmacyStockDTO> getLowStockAlerts();
    List<PharmacyStockDTO> getExpiringMedications();
    
    // Analytics
    Map<String, Object> getAppointmentAnalytics(LocalDate startDate, LocalDate endDate);
    Map<String, Object> getPrescriptionAnalytics(LocalDate startDate, LocalDate endDate);
    Map<String, Object> getRevenueAnalytics(LocalDate startDate, LocalDate endDate);
    List<TrendDataDTO> getTrendAnalysis(String metricType, int months);
    
    // Reports
    ExecutiveSummaryDTO getExecutiveSummary();
    MonthlyReportDTO getMonthlyReport(int year, int month);
    QuarterlyReportDTO getQuarterlyReport(int year, int quarter);
    AnnualReportDTO getAnnualReport(int year);
}

@Service
@RequiredArgsConstructor
public class DirectorServiceImpl implements IDirectorService {
    
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PharmacyStockRepository stockRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    
    @Override
    public DirectorDashboardDTO getExecutiveDashboard() {
        DirectorDashboardDTO dashboard = new DirectorDashboardDTO();
        
        // Key metrics
        dashboard.setTotalDoctors(doctorRepository.count());
        dashboard.setTotalPatients(patientRepository.count());
        dashboard.setTotalAppointmentsToday(
            appointmentRepository.countByDate(LocalDate.now()));
        dashboard.setTotalAppointmentsThisMonth(
            appointmentRepository.countByMonth(LocalDate.now()));
        
        // Performance metrics
        dashboard.setAverageAppointmentsPerDoctor(
            calculateAverageAppointmentsPerDoctor());
        dashboard.setPatientSatisfactionRate(
            calculatePatientSatisfactionRate());
        dashboard.setDoctorUtilizationRate(
            calculateDoctorUtilizationRate());
        
        // Financial metrics
        dashboard.setMonthlyRevenue(calculateMonthlyRevenue());
        dashboard.setRevenueGrowth(calculateRevenueGrowth());
        
        // Pharmacy metrics
        dashboard.setLowStockItemsCount(stockRepository.countLowStockItems());
        dashboard.setExpiringMedicationsCount(
            stockRepository.countExpiringMedications(LocalDate.now().plusMonths(3)));
        
        return dashboard;
    }
    
    @Override
    public Map<String, Object> getKPIs() {
        Map<String, Object> kpis = new HashMap<>();
        
        // Operational KPIs
        kpis.put("appointmentCompletionRate", calculateAppointmentCompletionRate());
        kpis.put("averageWaitTime", calculateAverageWaitTime());
        kpis.put("patientRetentionRate", calculatePatientRetentionRate());
        kpis.put("doctorProductivity", calculateDoctorProductivity());
        
        // Quality KPIs
        kpis.put("prescriptionAccuracy", calculatePrescriptionAccuracy());
        kpis.put("patientSatisfaction", calculatePatientSatisfactionRate());
        kpis.put("treatmentSuccessRate", calculateTreatmentSuccessRate());
        
        // Financial KPIs
        kpis.put("revenuePerPatient", calculateRevenuePerPatient());
        kpis.put("operatingMargin", calculateOperatingMargin());
        kpis.put("costPerAppointment", calculateCostPerAppointment());
        
        return kpis;
    }
    
    @Override
    public DoctorPerformanceDTO getDoctorPerformance(Long doctorId) {
        DoctorPerformanceDTO performance = new DoctorPerformanceDTO();
        
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        
        performance.setDoctorId(doctorId);
        performance.setDoctorName(doctor.getUsername());
        performance.setSpecialization(doctor.getSpecialization());
        
        // Performance metrics
        performance.setTotalPatients(
            patientRepository.countByDoctorId(doctorId));
        performance.setTotalAppointments(
            appointmentRepository.countByDoctorId(doctorId));
        performance.setCompletedAppointments(
            appointmentRepository.countByDoctorIdAndStatus(
                doctorId, AppointmentStatus.COMPLETED));
        performance.setCancelledAppointments(
            appointmentRepository.countByDoctorIdAndStatus(
                doctorId, AppointmentStatus.CANCELLED));
        performance.setNoShowAppointments(
            appointmentRepository.countByDoctorIdAndStatus(
                doctorId, AppointmentStatus.NO_SHOW));
        
        // Calculate rates
        long total = performance.getTotalAppointments();
        if (total > 0) {
            performance.setCompletionRate(
                (double) performance.getCompletedAppointments() / total * 100);
            performance.setCancellationRate(
                (double) performance.getCancelledAppointments() / total * 100);
            performance.setNoShowRate(
                (double) performance.getNoShowAppointments() / total * 100);
        }
        
        performance.setAveragePatientsPerDay(
            calculateAveragePatientsPerDay(doctorId));
        performance.setPrescriptionsWritten(
            prescriptionRepository.countByDoctorId(doctorId));
        
        return performance;
    }
    
    @Override
    public Map<String, Object> getPharmacyInventory() {
        Map<String, Object> inventory = new HashMap<>();
        
        List<PharmacyStock> allStock = stockRepository.findAll();
        
        inventory.put("totalMedications", allStock.size());
        inventory.put("totalValue", calculateTotalInventoryValue(allStock));
        inventory.put("lowStockItems", stockRepository.countLowStockItems());
        inventory.put("expiringItems", 
            stockRepository.countExpiringMedications(LocalDate.now().plusMonths(3)));
        inventory.put("stockByCategory", groupStockByCategory(allStock));
        inventory.put("topMedications", getTopMedicationsByUsage());
        
        return inventory;
    }
    
    @Override
    public Map<String, Object> getAppointmentAnalytics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        List<Appointment> appointments = 
            appointmentRepository.findByDateBetween(startDate, endDate);
        
        analytics.put("totalAppointments", appointments.size());
        analytics.put("byStatus", groupAppointmentsByStatus(appointments));
        analytics.put("byType", groupAppointmentsByType(appointments));
        analytics.put("byDoctor", groupAppointmentsByDoctor(appointments));
        analytics.put("byDayOfWeek", groupAppointmentsByDayOfWeek(appointments));
        analytics.put("byTimeSlot", groupAppointmentsByTimeSlot(appointments));
        analytics.put("averagePerDay", appointments.size() / 
            ChronoUnit.DAYS.between(startDate, endDate));
        
        return analytics;
    }
    
    @Override
    public ExecutiveSummaryDTO getExecutiveSummary() {
        ExecutiveSummaryDTO summary = new ExecutiveSummaryDTO();
        
        summary.setGeneratedDate(LocalDateTime.now());
        summary.setPeriod("Current Month");
        
        // Operational summary
        summary.setTotalDoctors(doctorRepository.count());
        summary.setTotalPatients(patientRepository.count());
        summary.setTotalAppointments(
            appointmentRepository.countByMonth(LocalDate.now()));
        summary.setCompletedAppointments(
            appointmentRepository.countByMonthAndStatus(
                LocalDate.now(), AppointmentStatus.COMPLETED));
        
        // Financial summary
        summary.setTotalRevenue(calculateMonthlyRevenue());
        summary.setRevenueGrowth(calculateRevenueGrowth());
        summary.setOperatingCosts(calculateOperatingCosts());
        summary.setNetProfit(summary.getTotalRevenue() - summary.getOperatingCosts());
        
        // Quality metrics
        summary.setPatientSatisfactionScore(calculatePatientSatisfactionRate());
        summary.setDoctorUtilizationRate(calculateDoctorUtilizationRate());
        summary.setAppointmentCompletionRate(calculateAppointmentCompletionRate());
        
        // Pharmacy summary
        summary.setTotalPrescriptions(
            prescriptionRepository.countByMonth(LocalDate.now()));
        summary.setDispensedPrescriptions(
            prescriptionRepository.countByMonthAndStatus(
                LocalDate.now(), PrescriptionStatus.DISPENSED));
        summary.setPharmacyRevenue(calculatePharmacyRevenue());
        
        // Trends
        summary.setPatientGrowthRate(calculatePatientGrowthRate());
        summary.setAppointmentTrend(getAppointmentTrend());
        summary.setRevenueTrend(getRevenueTrend());
        
        // Alerts and recommendations
        summary.setLowStockAlerts(stockRepository.countLowStockItems());
        summary.setExpiringMedicationsAlerts(
            stockRepository.countExpiringMedications(LocalDate.now().plusMonths(3)));
        summary.setRecommendations(generateRecommendations());
        
        return summary;
    }
    
    // Helper methods
    private double calculateAverageAppointmentsPerDoctor() {
        long totalDoctors = doctorRepository.count();
        long totalAppointments = appointmentRepository.countByMonth(LocalDate.now());
        return totalDoctors > 0 ? (double) totalAppointments / totalDoctors : 0;
    }
    
    private double calculateDoctorUtilizationRate() {
        // Calculate based on scheduled vs available time slots
        // Implementation depends on business logic
        return 75.5; // Example value
    }
    
    private double calculatePatientSatisfactionRate() {
        // Would integrate with patient feedback system
        return 92.3; // Example value
    }
    
    private double calculateMonthlyRevenue() {
        // Calculate from appointments, prescriptions, etc.
        return 150000.0; // Example value
    }
    
    private double calculateRevenueGrowth() {
        // Compare current month to previous month
        return 8.5; // Example: 8.5% growth
    }
    
    private List<String> generateRecommendations() {
        List<String> recommendations = new ArrayList<>();
        
        if (stockRepository.countLowStockItems() > 5) {
            recommendations.add("Reorder medications - " + 
                stockRepository.countLowStockItems() + " items below reorder level");
        }
        
        if (calculateDoctorUtilizationRate() < 70) {
            recommendations.add("Doctor utilization below target - consider scheduling optimization");
        }
        
        if (calculateAppointmentCompletionRate() < 85) {
            recommendations.add("High cancellation rate - review appointment confirmation process");
        }
        
        return recommendations;
    }
}
```

---
