# Spring Boot Backend Builder Skill

## Purpose
This skill provides a comprehensive guide for building Spring Boot REST API backends using the layered architecture pattern, design patterns, and best practices demonstrated in the JEE TPs and TP_J2E project.

## When to Use This Skill
Activate this skill when:
- Building a new Spring Boot REST API backend
- Creating CRUD operations with JPA entities
- Implementing layered architecture (Controllers, Services, Repositories, Entities)
- Setting up database relationships and mappings
- Developing RESTful endpoints following best practices

---

## Project Setup Checklist

### 1. Maven Dependencies (pom.xml)
```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
    
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Database Driver (MySQL example) -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Lombok (optional but recommended) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- DevTools (optional) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 2. Application Configuration (application.properties)
```properties
# Application name
spring.application.name=your-app-name

# Database connection
spring.datasource.url=jdbc:mysql://localhost:3306/your_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password

# JPA/Hibernate configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Server configuration
server.port=8080
```

**DDL Auto Options:**
- `create` - Drop and recreate schema (data loss!)
- `create-drop` - Drop on shutdown
- `update` - Update schema safely (recommended for development)
- `validate` - Only validate schema
- `none` - No automatic schema management

---

## Architecture: 4-Layer Pattern

```
┌─────────────────────────────────────────┐
│     Controllers Layer                   │  @RestController
│  (HTTP Request/Response Handling)       │  @RequestMapping
├─────────────────────────────────────────┤
│     Services Layer                      │  @Service
│  (Business Logic & Transactions)        │  @Transactional
├─────────────────────────────────────────┤
│     Repositories Layer                  │  extends JpaRepository
│  (Data Access & Queries)                │  @Repository
├─────────────────────────────────────────┤
│     Entities Layer                      │  @Entity
│  (Domain Objects & Persistence)         │  JPA Annotations
└─────────────────────────────────────────┘
```

---

## Step-by-Step Implementation Guide

### Step 1: Create Entity Classes

**Package:** `com.yourcompany.yourapp.entities`

**Template:**
```java
package com.yourcompany.yourapp.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data  // or use @Getter @Setter separately
@NoArgsConstructor
@AllArgsConstructor
public class YourEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Column(name = "custom_column_name")
    private String customField;
    
    private LocalDate createdDate;
    
    // Add relationships here (see relationship patterns below)
}
```

**Key Annotations:**
- `@Entity` - Marks class as JPA entity
- `@Id` - Primary key
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` - Auto-increment
- `@Column` - Custom column mapping
- `@Data` - Lombok: generates getters, setters, toString, equals, hashCode
- `@NoArgsConstructor` - No-argument constructor
- `@AllArgsConstructor` - Constructor with all fields

### Step 2: Define Relationships

#### One-to-Many / Many-to-One
```java
// Parent entity (One side)
@Entity
@Data
public class Parent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(mappedBy = "parent")
    private List<Child> children;
}

// Child entity (Many side)
@Entity
@Data
public class Child {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private Parent parent;
}
```

#### Many-to-Many
```java
// Owning side
@Entity
@Data
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToMany
    private List<Course> courses;
}

// Inverse side
@Entity
@Data
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToMany(mappedBy = "courses")
    private List<Student> students;
}
```

#### One-to-One
```java
// Owning side
@Entity
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    private Profile profile;
}

// Inverse side
@Entity
@Data
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(mappedBy = "profile")
    private User user;
}
```

### Step 3: Create Repository Interfaces

**Package:** `com.yourcompany.yourapp.repositories`

**Template:**
```java
package com.yourcompany.yourapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.yourcompany.yourapp.entities.YourEntity;
import java.util.List;

@Repository
public interface YourEntityRepository extends JpaRepository<YourEntity, Long> {
    
    // Query method naming conventions (Spring generates implementation)
    List<YourEntity> findByName(String name);
    List<YourEntity> findByNameContaining(String keyword);
    List<YourEntity> findByOrderByNameAsc();
    List<YourEntity> findByNameAndAge(String name, Integer age);
    
    // Custom JPQL query
    @Query("SELECT e FROM YourEntity e WHERE e.field > :value")
    List<YourEntity> customQuery(String value);
    
    // Native SQL query
    @Query(value = "SELECT * FROM your_entity WHERE field = ?1", nativeQuery = true)
    List<YourEntity> nativeQuery(String value);
}
```

**Query Method Keywords:**
- `findBy`, `getBy`, `queryBy` - Start query
- `And`, `Or` - Logical operators
- `GreaterThan`, `LessThan`, `Between` - Comparisons
- `Like`, `Containing`, `StartingWith`, `EndingWith` - String matching
- `OrderBy...Asc`, `OrderBy...Desc` - Sorting
- `Distinct` - Remove duplicates
- `Top`, `First` - Limit results

### Step 4: Create Service Interface

**Package:** `com.yourcompany.yourapp.services`

**Template:**
```java
package com.yourcompany.yourapp.services;

import com.yourcompany.yourapp.entities.YourEntity;
import java.util.List;

public interface IYourEntityService {
    
    // CRUD operations
    void create(YourEntity entity);
    YourEntity findById(Long id);
    List<YourEntity> findAll();
    void update(YourEntity entity);
    void deleteById(Long id);
    
    // Business-specific operations
    List<YourEntity> findByCustomCriteria(String criteria);
    void performBusinessOperation(Long id, String param);
    String getStatistics();
}
```

### Step 5: Implement Service Class

**Package:** `com.yourcompany.yourapp.services`

**Template:**
```java
package com.yourcompany.yourapp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import com.yourcompany.yourapp.entities.YourEntity;
import com.yourcompany.yourapp.repositories.YourEntityRepository;
import java.util.List;

@Service
public class YourEntityServiceImpl implements IYourEntityService {
    
    @Autowired
    private YourEntityRepository repository;
    
    @Override
    public void create(YourEntity entity) {
        repository.save(entity);
    }
    
    @Override
    public YourEntity findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Entity not found with id: " + id));
    }
    
    @Override
    public List<YourEntity> findAll() {
        return repository.findAll();
    }
    
    @Override
    public void update(YourEntity entity) {
        if (repository.existsById(entity.getId())) {
            repository.save(entity);
        } else {
            throw new RuntimeException("Entity not found");
        }
    }
    
    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    @Override
    public List<YourEntity> findByCustomCriteria(String criteria) {
        return repository.findByNameContaining(criteria);
    }
    
    @Transactional
    @Override
    public void performBusinessOperation(Long id, String param) {
        YourEntity entity = repository.findById(id).orElseThrow();
        // Perform business logic
        entity.setName(param);
        repository.save(entity);
    }
    
    @Override
    public String getStatistics() {
        return "Total entities: " + repository.count();
    }
}
```

**Important:**
- Use `@Transactional` for operations that modify multiple entities
- Always handle `Optional` results from `findById()`
- Inject repositories, not other service implementations directly

### Step 6: Create REST Controller

**Package:** `com.yourcompany.yourapp.controllers`

**Template:**
```java
package com.yourcompany.yourapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.yourcompany.yourapp.entities.YourEntity;
import com.yourcompany.yourapp.services.IYourEntityService;
import java.util.List;

@RestController
@RequestMapping("/api/entities")
@CrossOrigin(origins = "*")  // Configure CORS as needed
public class YourEntityController {
    
    @Autowired
    private IYourEntityService service;
    
    // GET all
    @GetMapping
    public ResponseEntity<List<YourEntity>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }
    
    // GET by ID
    @GetMapping("/{id}")
    public ResponseEntity<YourEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    // POST - Create
    @PostMapping
    public ResponseEntity<String> create(@RequestBody YourEntity entity) {
        service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body("Created successfully");
    }
    
    // PUT - Update
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody YourEntity entity) {
        entity.setId(id);
        service.update(entity);
        return ResponseEntity.ok("Updated successfully");
    }
    
    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok("Deleted successfully");
    }
    
    // Custom endpoint with query parameter
    @GetMapping("/search")
    public ResponseEntity<List<YourEntity>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(service.findByCustomCriteria(keyword));
    }
    
    // Custom endpoint with path variable
    @PutMapping("/{id}/operation")
    public ResponseEntity<String> performOperation(@PathVariable Long id, @RequestParam String param) {
        service.performBusinessOperation(id, param);
        return ResponseEntity.ok("Operation completed");
    }
    
    // Statistics endpoint
    @GetMapping("/stats")
    public ResponseEntity<String> getStats() {
        return ResponseEntity.ok(service.getStatistics());
    }
}
```

**HTTP Method Mapping:**
- `@GetMapping` - Retrieve resources (READ)
- `@PostMapping` - Create new resource (CREATE)
- `@PutMapping` - Update existing resource (UPDATE)
- `@DeleteMapping` - Delete resource (DELETE)
- `@PatchMapping` - Partial update

**Parameter Binding:**
- `@PathVariable` - Extract from URL path: `/api/entities/{id}`
- `@RequestParam` - Extract from query string: `/api/entities?name=value`
- `@RequestBody` - Extract from request body (JSON)

**Response Types:**
- `ResponseEntity<T>` - Full control over HTTP response
- Direct return type - Spring handles response automatically

---

## Common Patterns and Best Practices

### 1. Exception Handling

**Global Exception Handler:**
```java
package com.yourcompany.yourapp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("An error occurred: " + ex.getMessage());
    }
}
```

### 2. DTO Pattern (Data Transfer Objects)

**Why:** Decouple API contracts from entity structure, avoid exposing sensitive data

```java
// DTO class
package com.yourcompany.yourapp.dto;

import lombok.Data;

@Data
public class YourEntityDTO {
    private Long id;
    private String name;
    // Only fields needed for API response
}

// Mapper utility
public class EntityMapper {
    public static YourEntityDTO toDTO(YourEntity entity) {
        YourEntityDTO dto = new YourEntityDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }
    
    public static YourEntity toEntity(YourEntityDTO dto) {
        YourEntity entity = new YourEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        return entity;
    }
}

// Use in controller
@GetMapping
public ResponseEntity<List<YourEntityDTO>> getAll() {
    return ResponseEntity.ok(
        service.findAll().stream()
            .map(EntityMapper::toDTO)
            .collect(Collectors.toList())
    );
}
```

### 3. Validation

```java
import jakarta.validation.constraints.*;

@Entity
@Data
public class YourEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Name cannot be null")
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @Min(value = 0, message = "Value must be positive")
    @Max(value = 100, message = "Value cannot exceed 100")
    private Integer score;
}

// In controller, use @Valid
@PostMapping
public ResponseEntity<String> create(@Valid @RequestBody YourEntity entity) {
    service.create(entity);
    return ResponseEntity.status(HttpStatus.CREATED).body("Created successfully");
}
```

### 4. Pagination and Sorting

```java
// Repository - no changes needed, JpaRepository supports pagination

// Service
public Page<YourEntity> findAllPaginated(int page, int size, String sortBy) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
    return repository.findAll(pageable);
}

// Controller
@GetMapping("/paginated")
public ResponseEntity<Page<YourEntity>> getAllPaginated(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "id") String sortBy) {
    
    return ResponseEntity.ok(service.findAllPaginated(page, size, sortBy));
}
```

### 5. Avoiding N+1 Query Problem

```java
// Use @EntityGraph or JOIN FETCH

// Option 1: @EntityGraph
@EntityGraph(attributePaths = {"courses", "university"})
List<Student> findAll();

// Option 2: JPQL with JOIN FETCH
@Query("SELECT s FROM Student s LEFT JOIN FETCH s.courses")
List<Student> findAllWithCourses();
```

### 6. Handling Circular Dependencies in JSON

```java
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

// Parent
@Entity
public class Parent {
    @OneToMany(mappedBy = "parent")
    @JsonManagedReference
    private List<Child> children;
}

// Child
@Entity
public class Child {
    @ManyToOne
    @JsonBackReference
    private Parent parent;
}

// Or simply ignore one side
@Entity
public class Child {
    @ManyToOne
    @JsonIgnore
    private Parent parent;
}
```

---

## Package Structure Template

```
src/main/java/com/yourcompany/yourapp/
├── YourAppApplication.java          # Main application class
├── controllers/                     # REST controllers
│   ├── EntityAController.java
│   └── EntityBController.java
├── services/                        # Business logic
│   ├── IEntityAService.java
│   ├── EntityAServiceImpl.java
│   ├── IEntityBService.java
│   └── EntityBServiceImpl.java
├── repositories/                    # Data access
│   ├── EntityARepository.java
│   └── EntityBRepository.java
├── entities/                        # JPA entities
│   ├── EntityA.java
│   └── EntityB.java
├── dto/                            # Data transfer objects (optional)
│   ├── EntityADTO.java
│   └── EntityBDTO.java
├── exceptions/                      # Custom exceptions
│   ├── GlobalExceptionHandler.java
│   └── CustomException.java
└── config/                         # Configuration classes
    └── CorsConfig.java

src/main/resources/
├── application.properties          # Configuration
└── data.sql                       # Initial data (optional)
```

---

## Testing Patterns

### Unit Test (Service Layer)
```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class YourEntityServiceTest {
    
    @Mock
    private YourEntityRepository repository;
    
    @InjectMocks
    private YourEntityServiceImpl service;
    
    @Test
    public void testCreate() {
        YourEntity entity = new YourEntity();
        entity.setName("Test");
        
        service.create(entity);
        
        verify(repository, times(1)).save(entity);
    }
    
    @Test
    public void testFindById() {
        YourEntity entity = new YourEntity();
        entity.setId(1L);
        
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        
        YourEntity result = service.findById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }
}
```

### Integration Test (Controller Layer)
```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class YourEntityControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testGetAll() throws Exception {
        mockMvc.perform(get("/api/entities"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"));
    }
}
```

---

## Common Pitfalls to Avoid

1. **Missing @Transactional** - Multi-entity operations need transaction management
2. **N+1 Query Problem** - Use JOIN FETCH or @EntityGraph
3. **Lazy Loading Outside Transaction** - Fetch data within @Transactional method
4. **Circular JSON References** - Use @JsonIgnore or DTOs
5. **Missing HTTP Annotations** - Every controller method needs @GetMapping, @PostMapping, etc.
6. **Not Handling Optional** - Always use .orElseThrow() or .orElse()
7. **Hardcoded Values** - Use application.properties for configuration
8. **No Exception Handling** - Implement @RestControllerAdvice
9. **Exposing Entities Directly** - Use DTOs for API responses
10. **Missing Validation** - Use @Valid and constraint annotations

---

## Quick Reference: Annotations

### Entity Layer
- `@Entity` - JPA entity
- `@Id` - Primary key
- `@GeneratedValue` - Auto-generate ID
- `@Column` - Column mapping
- `@OneToMany`, `@ManyToOne`, `@ManyToMany`, `@OneToOne` - Relationships
- `@JoinColumn` - Foreign key column
- `@JoinTable` - Join table for many-to-many

### Repository Layer
- `@Repository` - Repository stereotype
- `@Query` - Custom JPQL/SQL query
- `@Param` - Named parameter in query

### Service Layer
- `@Service` - Service stereotype
- `@Transactional` - Transaction management
- `@Autowired` - Dependency injection

### Controller Layer
- `@RestController` - REST controller
- `@RequestMapping` - Base path
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` - HTTP methods
- `@PathVariable` - Path parameter
- `@RequestParam` - Query parameter
- `@RequestBody` - Request body
- `@CrossOrigin` - CORS configuration

### Lombok
- `@Data` - Getters, setters, toString, equals, hashCode
- `@Getter`, `@Setter` - Individual accessors
- `@NoArgsConstructor` - No-arg constructor
- `@AllArgsConstructor` - All-args constructor
- `@RequiredArgsConstructor` - Constructor for final fields

---

## Workflow Summary

1. **Design** - Plan entities and relationships
2. **Entities** - Create entity classes with JPA annotations
3. **Repositories** - Create repository interfaces extending JpaRepository
4. **Services** - Create service interface and implementation
5. **Controllers** - Create REST controllers with HTTP mappings
6. **Configuration** - Set up application.properties
7. **Test** - Write unit and integration tests
8. **Run** - Start application and test endpoints

**Remember:** Always follow the layered architecture pattern and maintain separation of concerns between layers.
