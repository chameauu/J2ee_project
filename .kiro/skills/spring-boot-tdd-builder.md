---
name: Spring Boot Backend Builder with Vertical TDD
description: Comprehensive guide for building Spring Boot backends using vertical TDD approach, layered architecture, and Context7 for documentation
version: 2.0
tags: [spring-boot, tdd, backend, rest-api, jpa, architecture, context7]
---

# Spring Boot Backend Builder with Vertical TDD

## Overview

This skill guides you through building production-ready Spring Boot backends using:
- **Vertical TDD**: Write one test, implement it, repeat (feature-by-feature)
- **Layered Architecture**: Controller → Service → Repository → Entity
- **Spring Boot Best Practices**: Dependency injection, DTOs, exception handling
- **Context7 Integration**: Use MCP server for up-to-date Spring Boot documentation

---

## Core Principles

### 1. Vertical TDD Approach

**Philosophy**: Build one complete feature slice at a time, from test to implementation.

```
For each feature:
1. Write a failing test (Red)
2. Implement minimal code to pass (Green)
3. Refactor if needed (Refactor)
4. Move to next feature
```

**Benefits**:
- Immediate feedback on design
- Working features incrementally
- Better test coverage
- Reduced debugging time

### 2. Test-First Development Flow

```
Feature: "Get patient by ID"

Step 1: Write Controller Test (Integration Test)
Step 2: Implement Controller
Step 3: Write Service Test (Unit Test)
Step 4: Implement Service
Step 5: Write Repository Test (if custom query)
Step 6: Implement Repository
Step 7: Run all tests → Green
Step 8: Refactor if needed
```

---

## Using Context7 MCP Server

### When to Use Context7

**ALWAYS use Context7 when**:
- Starting a new Spring Boot project
- Implementing a new Spring feature (Security, JPA, etc.)
- Unsure about annotation usage
- Need current best practices
- Working with Spring Data queries
- Configuring application properties

### How to Use Context7

```typescript
// Step 1: Resolve library ID
mcp_Context7_resolve_library_id({
  libraryName: "Spring Boot",
  query: "How to create REST controller with validation"
})

// Step 2: Query documentation
mcp_Context7_query_docs({
  libraryId: "/spring-projects/spring-boot",
  query: "REST controller with request validation and exception handling"
})
```

### Common Context7 Queries

```
- "Spring Boot JPA entity relationships one-to-many"
- "Spring Security JWT authentication configuration"
- "Spring Data JPA custom query methods"
- "Spring Boot validation annotations"
- "Spring Boot application properties database configuration"
- "Spring Boot REST controller best practices"
- "Spring Boot service layer transaction management"
- "Spring Boot exception handling with @ControllerAdvice"
```

---

## Architecture Layers

### Layer 1: Entity (Domain Model)

**Purpose**: Represent database tables and business domain

```java
@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<MedicalRecord> medicalRecords;
}
```

**Key Patterns**:
- Use Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor)
- Define relationships with JPA annotations
- Use enums for fixed value sets
- Add validation constraints

### Layer 2: Repository (Data Access)

**Purpose**: Database operations using Spring Data JPA

```java
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    // Query method naming convention
    Optional<Patient> findByEmail(String email);
    List<Patient> findByLastNameContainingIgnoreCase(String lastName);
    
    // Custom JPQL query
    @Query("SELECT p FROM Patient p WHERE p.dateOfBirth > :date")
    List<Patient> findPatientsYoungerThan(@Param("date") LocalDate date);
    
    // Native SQL query
    @Query(value = "SELECT * FROM patients WHERE blood_type = ?1", nativeQuery = true)
    List<Patient> findByBloodType(String bloodType);
    
    // Count queries
    Long countByGender(Gender gender);
    
    // Exists queries
    boolean existsByEmail(String email);
}
```

**Key Patterns**:
- Extend JpaRepository<Entity, ID>
- Use query method naming conventions
- Add @Query for complex queries
- Use @Param for named parameters

### Layer 3: Service (Business Logic)

**Purpose**: Business logic, transaction management, DTO conversion

```java
// Interface
public interface IPatientService {
    PatientDTO createPatient(PatientDTO dto);
    PatientDTO getPatientById(Long id);
    List<PatientDTO> getAllPatients();
    PatientDTO updatePatient(Long id, PatientDTO dto);
    void deletePatient(Long id);
    Page<PatientDTO> searchPatients(String keyword, Pageable pageable);
}

// Implementation
@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements IPatientService {
    
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    
    @Override
    public PatientDTO createPatient(PatientDTO dto) {
        // Validate business rules
        if (patientRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        
        // Convert DTO to Entity
        Patient patient = patientMapper.toEntity(dto);
        
        // Save
        Patient saved = patientRepository.save(patient);
        
        // Convert back to DTO
        return patientMapper.toDTO(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return patientMapper.toDTO(patient);
    }
    
    @Override
    public PatientDTO updatePatient(Long id, PatientDTO dto) {
        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        
        // Update fields
        patient.setFirstName(dto.getFirstName());
        patient.setLastName(dto.getLastName());
        patient.setDateOfBirth(dto.getDateOfBirth());
        
        Patient updated = patientRepository.save(patient);
        return patientMapper.toDTO(updated);
    }
}
```

**Key Patterns**:
- Interface + Implementation
- @Transactional for transaction management
- @Transactional(readOnly = true) for read operations
- Use DTOs, never expose entities
- Throw custom exceptions
- Use constructor injection with @RequiredArgsConstructor

### Layer 4: Controller (REST API)

**Purpose**: Handle HTTP requests, validation, response formatting

```java
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Validated
public class PatientController {
    
    private final IPatientService patientService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PatientDTO> createPatient(
            @Valid @RequestBody PatientDTO patientDTO) {
        PatientDTO created = patientService.createPatient(patientDTO);
        return ResponseEntity.created(
            URI.create("/api/patients/" + created.getId())
        ).body(created);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatient(@PathVariable Long id) {
        PatientDTO patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }
    
    @GetMapping
    public ResponseEntity<Page<PatientDTO>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<PatientDTO> patients = patientService.getAllPatients(pageable);
        return ResponseEntity.ok(patients);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientDTO patientDTO) {
        PatientDTO updated = patientService.updatePatient(id, patientDTO);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Key Patterns**:
- Use @RestController (not @Controller)
- @RequestMapping for base path
- @Valid for validation
- Return ResponseEntity<T>
- Use proper HTTP status codes
- Support pagination with Pageable

---

## Vertical TDD Workflow

### Example: Implementing "Create Patient" Feature

#### Step 1: Write Controller Integration Test

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PatientControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void shouldCreatePatient() throws Exception {
        // Given
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setFirstName("John");
        patientDTO.setLastName("Doe");
        patientDTO.setEmail("john.doe@example.com");
        patientDTO.setDateOfBirth(LocalDate.of(1990, 1, 1));
        
        // When & Then
        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }
    
    @Test
    void shouldReturn400WhenInvalidData() throws Exception {
        // Given - invalid patient (missing required fields)
        PatientDTO patientDTO = new PatientDTO();
        
        // When & Then
        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isBadRequest());
    }
}
```

#### Step 2: Implement Controller (Minimal)

```java
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {
    
    private final IPatientService patientService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PatientDTO> createPatient(
            @Valid @RequestBody PatientDTO patientDTO) {
        PatientDTO created = patientService.createPatient(patientDTO);
        return ResponseEntity.created(
            URI.create("/api/patients/" + created.getId())
        ).body(created);
    }
}
```

#### Step 3: Write Service Unit Test

```java
@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {
    
    @Mock
    private PatientRepository patientRepository;
    
    @Mock
    private PatientMapper patientMapper;
    
    @InjectMocks
    private PatientServiceImpl patientService;
    
    @Test
    void shouldCreatePatient() {
        // Given
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        
        Patient patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Doe");
        
        Patient savedPatient = new Patient();
        savedPatient.setId(1L);
        savedPatient.setFirstName("John");
        savedPatient.setLastName("Doe");
        
        when(patientRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(patientMapper.toEntity(dto)).thenReturn(patient);
        when(patientRepository.save(patient)).thenReturn(savedPatient);
        when(patientMapper.toDTO(savedPatient)).thenReturn(dto);
        
        // When
        PatientDTO result = patientService.createPatient(dto);
        
        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        verify(patientRepository).save(patient);
    }
    
    @Test
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        PatientDTO dto = new PatientDTO();
        dto.setEmail("existing@example.com");
        
        when(patientRepository.existsByEmail(dto.getEmail())).thenReturn(true);
        
        // When & Then
        assertThrows(DuplicateResourceException.class, 
            () -> patientService.createPatient(dto));
    }
}
```

#### Step 4: Implement Service

```java
@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements IPatientService {
    
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    
    @Override
    public PatientDTO createPatient(PatientDTO dto) {
        if (patientRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        
        Patient patient = patientMapper.toEntity(dto);
        Patient saved = patientRepository.save(patient);
        return patientMapper.toDTO(saved);
    }
}
```

#### Step 5: Run Tests → Should Pass ✅

#### Step 6: Refactor if Needed

---

## DTOs and Mappers

### DTO Pattern

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {
    private Long id;
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50)
    private String lastName;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    private Gender gender;
    
    // Don't include sensitive data or relationships
}
```

### Mapper Pattern (MapStruct)

```java
@Mapper(componentModel = "spring")
public interface PatientMapper {
    
    PatientDTO toDTO(Patient patient);
    
    Patient toEntity(PatientDTO dto);
    
    List<PatientDTO> toDTOList(List<Patient> patients);
    
    // Custom mapping
    @Mapping(target = "medicalRecords", ignore = true)
    Patient toEntityWithoutRelations(PatientDTO dto);
}
```

---

## Exception Handling

### Custom Exceptions

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
```

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ValidationErrorResponse response = new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            errors,
            LocalDateTime.now()
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

## Testing Strategy

### Test Pyramid

```
        /\
       /  \      E2E Tests (Few)
      /____\     
     /      \    Integration Tests (Some)
    /________\   
   /          \  Unit Tests (Many)
  /____________\ 
```

### Unit Tests (Service Layer)

```java
@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {
    
    @Mock
    private PatientRepository patientRepository;
    
    @InjectMocks
    private PatientServiceImpl patientService;
    
    @Test
    void testBusinessLogic() {
        // Test service methods with mocked dependencies
    }
}
```

### Integration Tests (Controller Layer)

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PatientControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testEndToEndFlow() {
        // Test HTTP endpoints with real database
    }
}
```

### Repository Tests (Optional)

```java
@DataJpaTest
class PatientRepositoryTest {
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Test
    void testCustomQuery() {
        // Test custom repository methods
    }
}
```

---

## Configuration

### application.properties

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_db
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Validation
spring.jackson.deserialization.fail-on-unknown-properties=true

# Logging
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### application-test.properties

```properties
# Test Database (H2)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
```

---

## Development Workflow

### For Each New Feature:

1. **Consult Context7**
   ```
   Query: "Spring Boot [feature] best practices"
   Example: "Spring Boot REST pagination and sorting"
   ```

2. **Write Integration Test First**
   - Test the HTTP endpoint
   - Test happy path
   - Test error cases

3. **Implement Controller**
   - Minimal code to compile
   - Delegate to service

4. **Write Service Unit Test**
   - Mock dependencies
   - Test business logic
   - Test edge cases

5. **Implement Service**
   - Business logic
   - DTO conversion
   - Exception handling

6. **Write Repository Test (if needed)**
   - Only for custom queries

7. **Implement Repository**
   - Query methods or @Query

8. **Run All Tests**
   - Should be green ✅

9. **Refactor**
   - Clean up code
   - Extract methods
   - Improve naming

10. **Commit**
    - One feature per commit

---

## Common Patterns

### Pagination

```java
@GetMapping
public ResponseEntity<Page<PatientDTO>> getPatients(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String direction) {
    
    Sort.Direction sortDirection = Sort.Direction.fromString(direction);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    
    Page<PatientDTO> patients = patientService.getAllPatients(pageable);
    return ResponseEntity.ok(patients);
}
```

### Search/Filter

```java
@GetMapping("/search")
public ResponseEntity<List<PatientDTO>> searchPatients(
        @RequestParam(required = false) String firstName,
        @RequestParam(required = false) String lastName,
        @RequestParam(required = false) String email) {
    
    List<PatientDTO> patients = patientService.searchPatients(
        firstName, lastName, email
    );
    return ResponseEntity.ok(patients);
}
```

### Soft Delete

```java
@Entity
public class Patient {
    @Column(name = "deleted")
    private Boolean deleted = false;
    
    private LocalDateTime deletedAt;
}

// Service
public void softDelete(Long id) {
    Patient patient = patientRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    patient.setDeleted(true);
    patient.setDeletedAt(LocalDateTime.now());
    patientRepository.save(patient);
}
```

### Audit Fields

```java
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Patient {
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @CreatedBy
    private String createdBy;
    
    @LastModifiedBy
    private String lastModifiedBy;
}
```

---

## Security Considerations

### JWT Authentication (Use Context7)

```
Query Context7: "Spring Security JWT authentication configuration"
```

### Role-Based Access Control

```java
@PreAuthorize("hasRole('DOCTOR')")
@GetMapping("/patients")
public ResponseEntity<List<PatientDTO>> getPatients() {
    // Only doctors can access
}

@PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
@PostMapping("/patients")
public ResponseEntity<PatientDTO> createPatient(@RequestBody PatientDTO dto) {
    // Doctors and admins can create
}
```

---

## Performance Optimization

### N+1 Query Problem

```java
// Bad - N+1 queries
@OneToMany(mappedBy = "patient")
private List<MedicalRecord> medicalRecords;

// Good - Use JOIN FETCH
@Query("SELECT p FROM Patient p LEFT JOIN FETCH p.medicalRecords WHERE p.id = :id")
Optional<Patient> findByIdWithRecords(@Param("id") Long id);
```

### Caching

```java
@Cacheable(value = "patients", key = "#id")
public PatientDTO getPatientById(Long id) {
    // Cached result
}

@CacheEvict(value = "patients", key = "#id")
public void deletePatient(Long id) {
    // Invalidate cache
}
```

---

## Checklist for Each Feature

- [ ] Consulted Context7 for best practices
- [ ] Written integration test (controller)
- [ ] Written unit test (service)
- [ ] Implemented controller
- [ ] Implemented service
- [ ] Implemented repository (if needed)
- [ ] All tests pass ✅
- [ ] Added validation
- [ ] Added exception handling
- [ ] Used DTOs (not entities)
- [ ] Added proper HTTP status codes
- [ ] Documented with comments
- [ ] Refactored code
- [ ] Committed changes

---

## Quick Reference

### Annotations Cheat Sheet

```java
// Entity Layer
@Entity, @Table, @Id, @GeneratedValue
@Column, @OneToMany, @ManyToOne, @ManyToMany
@Enumerated, @Temporal, @Lob

// Repository Layer
@Repository, @Query, @Param, @Modifying

// Service Layer
@Service, @Transactional, @Transactional(readOnly = true)

// Controller Layer
@RestController, @RequestMapping, @GetMapping, @PostMapping
@PutMapping, @DeleteMapping, @PathVariable, @RequestParam
@RequestBody, @Valid, @ResponseStatus

// Validation
@NotNull, @NotBlank, @NotEmpty, @Size, @Min, @Max
@Email, @Past, @Future, @Pattern

// Exception Handling
@RestControllerAdvice, @ExceptionHandler

// Testing
@SpringBootTest, @AutoConfigureMockMvc, @DataJpaTest
@ExtendWith(MockitoExtension.class), @Mock, @InjectMocks
```

---

## Remember

1. **Always use Context7** when unsure about Spring Boot features
2. **Write tests first** (vertical TDD)
3. **One feature at a time** (complete vertical slice)
4. **Use DTOs** (never expose entities)
5. **Handle exceptions** (global exception handler)
6. **Validate input** (@Valid annotations)
7. **Use transactions** (@Transactional)
8. **Follow naming conventions** (query methods)
9. **Keep controllers thin** (delegate to services)
10. **Test everything** (unit + integration)

---

## Example Context7 Workflow

```typescript
// 1. Starting new feature: "Add appointment scheduling"
mcp_Context7_resolve_library_id({
  libraryName: "Spring Boot",
  query: "REST API appointment scheduling with validation"
})

// 2. Get specific documentation
mcp_Context7_query_docs({
  libraryId: "/spring-projects/spring-boot",
  query: "How to implement REST endpoint with date validation and conflict checking"
})

// 3. Implement based on documentation
// 4. Write tests
// 5. Run tests
// 6. Refactor
```

---

This skill ensures you build robust, tested, maintainable Spring Boot backends following industry best practices with vertical TDD approach.
