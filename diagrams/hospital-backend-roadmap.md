# Hospital Management System - Backend Development Roadmap

## Project Overview

Building a multi-tenant hospital management system backend using:
- Spring Boot 3.x
- Vertical TDD approach
- Layered architecture
- Context7 for documentation
- MySQL database

**Estimated Timeline**: 12-16 weeks

---

## Phase 0: Project Setup (Week 1)

### 0.1 Initialize Spring Boot Project

**Tasks**:
- [ ] Create Spring Boot project with Spring Initializr
- [ ] Add dependencies: Web, JPA, MySQL, Validation, Lombok, Security, Test
- [ ] Configure application.properties (database, JPA, logging)
- [ ] Set up project structure (packages)
- [ ] Configure Git repository
- [ ] Set up CI/CD pipeline (optional)

**Dependencies**:
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**Context7 Queries**:
- "Spring Boot 3 project structure best practices"
- "Spring Boot application properties configuration for MySQL"

**Deliverables**:
- ✅ Running Spring Boot application
- ✅ Database connection configured
- ✅ Package structure created
- ✅ First test passing (ApplicationTests)

---

## Phase 1: Core Infrastructure (Week 2)

### 1.1 Multi-Tenancy Setup

**Tasks**:
- [ ] Create TenantContext (ThreadLocal)
- [ ] Create TenantInterceptor
- [ ] Create TenantRoutingDataSource
- [ ] Configure multi-tenant data sources
- [ ] Write tests for tenant isolation

**Context7 Queries**:
- "Spring Boot multi-tenancy separate schema implementation"
- "ThreadLocal context management in Spring Boot"

**Vertical TDD**:
1. Write test: Tenant context should be set from header
2. Implement: TenantInterceptor
3. Write test: Data source should route based on tenant
4. Implement: TenantRoutingDataSource

**Deliverables**:
- ✅ Tenant context management
- ✅ Dynamic data source routing
- ✅ Tests for tenant isolation

### 1.2 Exception Handling

**Tasks**:
- [ ] Create custom exceptions (ResourceNotFoundException, DuplicateResourceException, etc.)
- [ ] Create ErrorResponse DTOs
- [ ] Implement GlobalExceptionHandler with @RestControllerAdvice
- [ ] Write tests for exception handling

**Context7 Queries**:
- "Spring Boot global exception handling with @ControllerAdvice"
- "REST API error response best practices"

**Vertical TDD**:
1. Write test: Should return 404 for ResourceNotFoundException
2. Implement: GlobalExceptionHandler
3. Write test: Should return 400 for validation errors
4. Implement: Validation exception handling

**Deliverables**:
- ✅ Global exception handler
- ✅ Custom exceptions
- ✅ Consistent error responses

### 1.3 Base Entities and Enums

**Tasks**:
- [ ] Create enums (UserRole, Gender, AppointmentStatus, etc.)
- [ ] Create base User entity (abstract)
- [ ] Configure JPA inheritance strategy
- [ ] Write repository tests

**Context7 Queries**:
- "JPA inheritance strategies joined table vs single table"
- "Spring Data JPA enum mapping best practices"

**Deliverables**:
- ✅ All enums created
- ✅ Base User entity
- ✅ Inheritance configured

---

## Phase 2: User Management Module (Week 3-4)

### 2.1 Patient Entity and CRUD

**Feature**: Create Patient

**Vertical TDD Steps**:
1. **Write Integration Test** (PatientControllerIntegrationTest)
   ```java
   @Test
   void shouldCreatePatient() throws Exception {
       // POST /api/patients
       // Expect 201 CREATED
   }
   ```

2. **Implement Controller** (PatientController)
   ```java
   @PostMapping
   public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientDTO dto)
   ```

3. **Write Service Test** (PatientServiceImplTest)
   ```java
   @Test
   void shouldCreatePatient() {
       // Mock repository
       // Test business logic
   }
   ```

4. **Implement Service** (PatientServiceImpl)

5. **Create Repository** (PatientRepository)

6. **Create DTO and Mapper** (PatientDTO, PatientMapper)

7. **Run all tests** → Green ✅

**Tasks**:
- [ ] Patient entity with all fields
- [ ] PatientRepository with custom queries
- [ ] PatientService (interface + implementation)
- [ ] PatientController with CRUD endpoints
- [ ] PatientDTO and validation
- [ ] PatientMapper (MapStruct)
- [ ] Integration tests
- [ ] Unit tests

**Endpoints**:
- POST /api/patients (Create)
- GET /api/patients/{id} (Read)
- GET /api/patients (List with pagination)
- PUT /api/patients/{id} (Update)
- DELETE /api/patients/{id} (Delete)
- GET /api/patients/search (Search)

**Context7 Queries**:
- "Spring Boot REST CRUD operations best practices"
- "Spring Data JPA pagination and sorting"
- "MapStruct entity to DTO mapping"

**Deliverables**:
- ✅ Complete Patient CRUD
- ✅ All tests passing
- ✅ Pagination working
- ✅ Search functionality

### 2.2 Doctor Entity and CRUD

**Repeat Vertical TDD for Doctor**:
- Same process as Patient
- Additional fields: specialization, licenseNumber, yearsOfExperience

**Tasks**:
- [ ] Doctor entity
- [ ] DoctorRepository
- [ ] DoctorService
- [ ] DoctorController
- [ ] DoctorDTO and mapper
- [ ] Tests (integration + unit)

**Endpoints**:
- POST /api/doctors
- GET /api/doctors/{id}
- GET /api/doctors
- PUT /api/doctors/{id}
- DELETE /api/doctors/{id}
- GET /api/doctors/search?specialization=X

**Deliverables**:
- ✅ Complete Doctor CRUD
- ✅ Search by specialization
- ✅ All tests passing

### 2.3 Pharmacist and Administrator Entities

**Tasks**:
- [ ] Pharmacist entity and CRUD
- [ ] Administrator entity and CRUD
- [ ] HospitalDirector entity and CRUD
- [ ] Tests for all

**Deliverables**:
- ✅ All user types implemented
- ✅ CRUD operations for each
- ✅ Tests passing

---

## Phase 3: Authentication & Authorization (Week 5)

### 3.1 JWT Authentication

**Tasks**:
- [ ] Create JwtTokenProvider
- [ ] Create JwtAuthenticationFilter
- [ ] Create AuthService
- [ ] Create AuthController (login, register, refresh)
- [ ] Configure Spring Security
- [ ] Write authentication tests

**Context7 Queries**:
- "Spring Security JWT authentication implementation"
- "Spring Boot JWT token generation and validation"
- "Spring Security filter chain configuration"

**Vertical TDD**:
1. Write test: Should authenticate user and return JWT
2. Implement: AuthController.login()
3. Write test: Should validate JWT token
4. Implement: JwtTokenProvider
5. Write test: Should filter requests with JWT
6. Implement: JwtAuthenticationFilter

**Endpoints**:
- POST /api/auth/login
- POST /api/auth/register
- POST /api/auth/refresh-token
- POST /api/auth/logout
- GET /api/auth/me

**Deliverables**:
- ✅ JWT authentication working
- ✅ Login/register endpoints
- ✅ Token validation
- ✅ Tests passing

### 3.2 Role-Based Access Control

**Tasks**:
- [ ] Configure method security (@PreAuthorize)
- [ ] Add role checks to endpoints
- [ ] Write authorization tests

**Context7 Queries**:
- "Spring Security method level authorization"
- "Spring Boot @PreAuthorize role-based access"

**Deliverables**:
- ✅ Role-based access control
- ✅ Endpoints secured by role
- ✅ Authorization tests passing

---

## Phase 4: Medical Records Module (Week 6-7)

### 4.1 MedicalRecord Entity

**Vertical TDD for MedicalRecord**:

**Tasks**:
- [ ] MedicalRecord entity with relationships
- [ ] MedicalRecordRepository
- [ ] MedicalRecordService
- [ ] MedicalRecordController
- [ ] MedicalRecordDTO and mapper
- [ ] Tests

**Relationships**:
- ManyToOne with Patient
- ManyToOne with Doctor
- OneToMany with Prescription

**Endpoints**:
- POST /api/medical-records (Create)
- GET /api/medical-records/{id} (Read)
- PUT /api/medical-records/{id} (Update with notes)
- GET /api/patients/{patientId}/medical-records (Get patient history)
- GET /api/doctors/{doctorId}/medical-records (Get doctor's records)

**Context7 Queries**:
- "JPA one-to-many bidirectional relationship"
- "Spring Data JPA fetch strategies"
- "Avoiding N+1 query problem in JPA"

**Deliverables**:
- ✅ MedicalRecord CRUD
- ✅ Relationships working
- ✅ No N+1 queries
- ✅ Tests passing

### 4.2 Doctor Views Patient Files

**Feature**: Doctor can view patient medical history

**Vertical TDD**:
1. Write test: GET /api/doctors/patients/{id} should return patient with records
2. Implement: DoctorController.getPatientById()
3. Write test: Service should fetch patient with medical records
4. Implement: DoctorService.getPatientById()
5. Run tests → Green ✅

**Endpoints**:
- GET /api/doctors/patients (List all patients)
- GET /api/doctors/patients/{id} (Get patient details with records)
- GET /api/doctors/patients/{id}/records (Get patient medical history)
- PUT /api/doctors/medical-records/{id} (Update record with notes)

**Deliverables**:
- ✅ Doctor can view patients
- ✅ Doctor can view medical history
- ✅ Doctor can add notes
- ✅ Tests passing

---

## Phase 5: Appointment Module (Week 8)

### 5.1 Appointment Entity and Management

**Vertical TDD for Appointments**:

**Tasks**:
- [ ] Appointment entity
- [ ] AppointmentRepository with custom queries
- [ ] AppointmentService
- [ ] AppointmentController
- [ ] AppointmentDTO and mapper
- [ ] Tests

**Business Rules**:
- Only Admin can create appointments
- Doctor can view and update status
- Patient can view only (read-only)
- Check for time slot conflicts

**Endpoints**:
- POST /api/admin/appointments (Admin creates)
- GET /api/appointments/{id} (Anyone can view their appointments)
- PUT /api/appointments/{id}/status (Doctor updates status)
- GET /api/doctors/appointments (Doctor's appointments)
- GET /api/patients/appointments (Patient's appointments)
- DELETE /api/admin/appointments/{id} (Admin cancels)

**Context7 Queries**:
- "Spring Data JPA date range queries"
- "JPA query for checking time slot conflicts"

**Vertical TDD**:
1. Write test: Admin should create appointment
2. Implement: AdminController.createAppointment()
3. Write test: Should check for time slot conflicts
4. Implement: AppointmentService.checkConflict()
5. Write test: Doctor should update status
6. Implement: DoctorController.updateAppointmentStatus()

**Deliverables**:
- ✅ Appointment CRUD
- ✅ Conflict checking
- ✅ Role-based access
- ✅ Tests passing

### 5.2 Doctor Views Appointments

**Feature**: Doctor views assigned appointments

**Endpoints**:
- GET /api/doctors/appointments (All appointments)
- GET /api/doctors/appointments/today (Today's appointments)
- GET /api/doctors/appointments/upcoming (Upcoming)
- PUT /api/doctors/appointments/{id}/status (Update status)

**Deliverables**:
- ✅ Doctor appointment views
- ✅ Status updates
- ✅ Tests passing

---

## Phase 6: Prescription Module (Week 9-10)

### 6.1 Medication and PharmacyStock

**Vertical TDD for Medication**:

**Tasks**:
- [ ] Medication entity
- [ ] MedicationRepository
- [ ] PharmacyStock entity
- [ ] PharmacyStockRepository
- [ ] MedicationService
- [ ] MedicationController
- [ ] Tests

**Endpoints**:
- POST /api/pharmacy/medications (Add medication)
- GET /api/pharmacy/medications (List all)
- GET /api/pharmacy/medications/{id} (Get one)
- PUT /api/pharmacy/medications/{id} (Update)
- POST /api/pharmacy/stock (Add stock)
- GET /api/pharmacy/stock (View inventory)
- GET /api/pharmacy/stock/low (Low stock alerts)

**Deliverables**:
- ✅ Medication management
- ✅ Stock management
- ✅ Low stock alerts
- ✅ Tests passing

### 6.2 Prescription Entity

**Vertical TDD for Prescriptions**:

**Tasks**:
- [ ] Prescription entity
- [ ] PrescriptionItem entity
- [ ] PrescriptionRepository
- [ ] PrescriptionItemRepository
- [ ] PrescriptionService
- [ ] PrescriptionController
- [ ] Tests

**Relationships**:
- ManyToOne with Patient
- ManyToOne with Doctor
- ManyToOne with MedicalRecord
- OneToMany with PrescriptionItem
- PrescriptionItem ManyToOne with Medication

**Endpoints**:
- POST /api/doctors/prescriptions (Doctor writes prescription)
- GET /api/prescriptions/{id} (View prescription)
- GET /api/patients/prescriptions (Patient's prescriptions)
- GET /api/doctors/prescriptions (Doctor's prescriptions)
- PUT /api/doctors/prescriptions/{id} (Update prescription)

**Context7 Queries**:
- "JPA cascade operations for parent-child relationships"
- "Spring Data JPA save with nested entities"

**Deliverables**:
- ✅ Prescription creation
- ✅ Prescription items
- ✅ Relationships working
- ✅ Tests passing

### 6.3 Pharmacist Dispenses Prescriptions

**Feature**: Pharmacist dispenses medication

**Vertical TDD**:
1. Write test: Pharmacist should dispense prescription
2. Implement: PharmacistController.dispensePrescription()
3. Write test: Should reduce stock when dispensing
4. Implement: PharmacyService.dispensePrescription()
5. Write test: Should throw error if insufficient stock
6. Implement: Stock validation logic

**Endpoints**:
- GET /api/pharmacy/prescriptions (All prescriptions)
- GET /api/pharmacy/prescriptions/pending (Pending only)
- PUT /api/pharmacy/prescriptions/{id}/dispense (Dispense)

**Business Logic**:
- Check stock availability
- Reduce stock quantity
- Mark items as dispensed
- Update prescription status

**Deliverables**:
- ✅ Dispense functionality
- ✅ Stock reduction
- ✅ Validation working
- ✅ Tests passing

---

## Phase 7: Statistics & Analytics (Week 11)

### 7.1 Admin Dashboard

**Tasks**:
- [ ] StatisticsService
- [ ] DashboardStatsDTO
- [ ] StatisticsController
- [ ] Aggregate queries
- [ ] Tests

**Endpoints**:
- GET /api/admin/dashboard (Dashboard stats)
- GET /api/admin/dashboard/overview (System overview)
- GET /api/admin/dashboard/charts (Chart data)

**Metrics**:
- Total doctors, patients, pharmacists
- Today's appointments
- Completed appointments
- Active prescriptions
- Total medical records

**Context7 Queries**:
- "Spring Data JPA aggregate queries count sum"
- "JPA native queries for complex statistics"

**Deliverables**:
- ✅ Dashboard statistics
- ✅ Chart data endpoints
- ✅ Tests passing

### 7.2 Doctor Statistics

**Endpoints**:
- GET /api/doctors/statistics (Doctor's stats)
- GET /api/doctors/statistics/patients-count
- GET /api/doctors/statistics/appointments-today
- GET /api/doctors/statistics/monthly-summary

**Deliverables**:
- ✅ Doctor statistics
- ✅ Performance metrics
- ✅ Tests passing

---

## Phase 8: Hospital Director Module (Week 12)

### 8.1 Director Dashboard

**Tasks**:
- [ ] DirectorService
- [ ] DirectorDashboardDTO
- [ ] DirectorController
- [ ] KPI calculations
- [ ] Tests

**Endpoints**:
- GET /api/director/dashboard (Executive dashboard)
- GET /api/director/dashboard/kpis (Key performance indicators)
- GET /api/director/doctors (All doctors)
- GET /api/director/doctors/{id}/performance (Doctor performance)
- GET /api/director/patients (All patients)
- GET /api/director/patients/demographics (Demographics)
- GET /api/director/pharmacy/inventory (Inventory overview)
- GET /api/director/analytics/appointments (Appointment analytics)
- GET /api/director/reports/executive-summary (Executive summary)

**KPIs**:
- Doctor utilization rate
- Patient satisfaction rate
- Appointment completion rate
- Revenue metrics
- Operational efficiency

**Deliverables**:
- ✅ Executive dashboard
- ✅ Performance analytics
- ✅ Reports generation
- ✅ Tests passing

---

## Phase 9: Advanced Features (Week 13-14)

### 9.1 Search and Filtering

**Tasks**:
- [ ] Implement search across entities
- [ ] Add filtering capabilities
- [ ] Optimize queries
- [ ] Tests

**Context7 Queries**:
- "Spring Data JPA specification for dynamic queries"
- "JPA Criteria API for complex filtering"

**Deliverables**:
- ✅ Search functionality
- ✅ Advanced filtering
- ✅ Tests passing

### 9.2 Audit and Logging

**Tasks**:
- [ ] Add audit fields (createdAt, updatedAt, createdBy, lastModifiedBy)
- [ ] Configure JPA auditing
- [ ] Add logging with SLF4J
- [ ] Tests

**Context7 Queries**:
- "Spring Data JPA auditing configuration"
- "Spring Boot logging best practices"

**Deliverables**:
- ✅ Audit fields on all entities
- ✅ Automatic audit tracking
- ✅ Comprehensive logging

### 9.3 Soft Delete

**Tasks**:
- [ ] Add deleted flag to entities
- [ ] Implement soft delete logic
- [ ] Filter deleted records in queries
- [ ] Tests

**Deliverables**:
- ✅ Soft delete implemented
- ✅ Deleted records filtered
- ✅ Tests passing

---

## Phase 10: Performance & Optimization (Week 15)

### 10.1 Query Optimization

**Tasks**:
- [ ] Identify N+1 query problems
- [ ] Add JOIN FETCH where needed
- [ ] Optimize slow queries
- [ ] Add database indexes
- [ ] Tests

**Context7 Queries**:
- "JPA N+1 query problem solutions"
- "Spring Data JPA fetch strategies"
- "Database indexing best practices"

**Deliverables**:
- ✅ No N+1 queries
- ✅ Optimized queries
- ✅ Proper indexes

### 10.2 Caching

**Tasks**:
- [ ] Configure Spring Cache
- [ ] Add @Cacheable to read operations
- [ ] Add @CacheEvict to write operations
- [ ] Configure Redis (optional)
- [ ] Tests

**Context7 Queries**:
- "Spring Boot caching with Redis"
- "Spring Cache annotations best practices"

**Deliverables**:
- ✅ Caching configured
- ✅ Performance improved
- ✅ Tests passing

---

## Phase 11: Documentation & Deployment (Week 16)

### 11.1 API Documentation

**Tasks**:
- [ ] Add Swagger/OpenAPI
- [ ] Document all endpoints
- [ ] Add examples
- [ ] Generate API docs

**Context7 Queries**:
- "Spring Boot Swagger OpenAPI 3 configuration"
- "Swagger API documentation best practices"

**Deliverables**:
- ✅ Swagger UI accessible
- ✅ All endpoints documented
- ✅ Examples provided

### 11.2 Docker & Deployment

**Tasks**:
- [ ] Create Dockerfile
- [ ] Create docker-compose.yml
- [ ] Configure environment variables
- [ ] Test deployment
- [ ] CI/CD pipeline

**Deliverables**:
- ✅ Docker image built
- ✅ Docker compose working
- ✅ Deployment successful

---

## Testing Strategy Throughout

### For Each Feature:

1. **Integration Test** (Controller layer)
   - Test HTTP endpoints
   - Test request/response
   - Test status codes

2. **Unit Test** (Service layer)
   - Mock dependencies
   - Test business logic
   - Test edge cases

3. **Repository Test** (Optional)
   - Test custom queries
   - Test relationships

### Test Coverage Goals:
- Overall: 80%+
- Service layer: 90%+
- Controller layer: 85%+

---

## Development Checklist (Per Feature)

- [ ] Consult Context7 for best practices
- [ ] Write integration test (RED)
- [ ] Implement controller (GREEN)
- [ ] Write service unit test (RED)
- [ ] Implement service (GREEN)
- [ ] Create repository if needed
- [ ] Create DTO and mapper
- [ ] Add validation
- [ ] Add exception handling
- [ ] Run all tests (GREEN)
- [ ] Refactor code
- [ ] Update documentation
- [ ] Commit changes

---

## Key Milestones

**Week 4**: User management complete ✅
**Week 5**: Authentication working ✅
**Week 7**: Medical records functional ✅
**Week 8**: Appointments working ✅
**Week 10**: Prescriptions complete ✅
**Week 12**: Director dashboard ready ✅
**Week 14**: Advanced features done ✅
**Week 16**: Production ready ✅

---

## Success Criteria

- [ ] All CRUD operations working
- [ ] All tests passing (80%+ coverage)
- [ ] Authentication and authorization working
- [ ] Multi-tenancy functional
- [ ] All user roles implemented
- [ ] API documented with Swagger
- [ ] Performance optimized
- [ ] Docker deployment ready
- [ ] No critical bugs
- [ ] Code reviewed and refactored

---

## Tools & Resources

**Development**:
- IntelliJ IDEA / VS Code
- Postman / Insomnia (API testing)
- MySQL Workbench
- Git

**Testing**:
- JUnit 5
- Mockito
- MockMvc
- H2 (test database)

**Documentation**:
- Context7 MCP server
- Spring Boot documentation
- Swagger UI

**Deployment**:
- Docker
- Docker Compose
- GitHub Actions (CI/CD)

---

## Next Steps

1. **Start with Phase 0**: Set up project
2. **Follow vertical TDD**: One feature at a time
3. **Use Context7**: For every Spring Boot question
4. **Test everything**: Write tests first
5. **Commit often**: One feature per commit
6. **Review regularly**: Code quality checks

---

## Notes

- Adjust timeline based on team size and experience
- Prioritize features based on business needs
- Can parallelize some modules (e.g., Pharmacy and Appointments)
- Regular code reviews recommended
- Deploy to staging environment early for feedback

**Remember**: Quality over speed. Better to have fewer features working perfectly than many features with bugs.
