# Hospital Management System - Backend

Multi-tenant hospital management system built with Spring Boot 3, following vertical TDD approach.

## Tech Stack

- **Java**: 17
- **Spring Boot**: 3.5.13
- **Database**: MySQL 8.0 (Production), H2 (Testing)
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, MockMvc

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- MySQL 8.0+
- IDE (IntelliJ IDEA / VS Code)

## Setup

### 1. Clone and Navigate
```bash
cd project
```

### 2. Configure Database
Create MySQL database:
```sql
CREATE DATABASE hospital_db;
```

Update `src/main/resources/application.properties` with your MySQL credentials:
```properties
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Build Project
```bash
mvn clean install
```

### 4. Run Application
```bash
mvn spring-boot:run
```

Application will start on `http://localhost:8080`

### 5. Run Tests
```bash
mvn test
```

## Project Structure

```
src/
├── main/
│   ├── java/com/hospital/management/
│   │   ├── HospitalManagementApplication.java
│   │   ├── config/              # Configuration classes
│   │   ├── multitenancy/        # Multi-tenancy setup
│   │   ├── entities/            # JPA entities
│   │   │   ├── user/
│   │   │   ├── medical/
│   │   │   ├── appointment/
│   │   │   ├── prescription/
│   │   │   └── pharmacy/
│   │   ├── repositories/        # Spring Data repositories
│   │   ├── services/            # Business logic
│   │   ├── controllers/         # REST controllers
│   │   ├── dto/                 # Data Transfer Objects
│   │   ├── mappers/             # MapStruct mappers
│   │   ├── security/            # Security & JWT
│   │   ├── exceptions/          # Custom exceptions
│   │   └── enums/               # Enumerations
│   └── resources/
│       └── application.properties
└── test/
    ├── java/com/hospital/management/
    │   ├── controllers/         # Integration tests
    │   └── services/            # Unit tests
    └── resources/
        └── application-test.properties
```

## Development Approach

This project follows **Vertical TDD** (Test-Driven Development):

1. Write integration test (Controller)
2. Write unit test (Service)
3. Implement minimal code to pass tests
4. Refactor
5. Repeat for next feature

## API Documentation

Once running, access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## Development Roadmap

See `../hospital-backend-roadmap.md` for detailed development plan.

## Architecture

- **Layered Architecture**: Controller → Service → Repository → Entity
- **Multi-Tenancy**: Separate schema per tenant
- **Security**: JWT-based authentication with role-based access control
- **Exception Handling**: Global exception handler with consistent error responses

## User Roles

- **DOCTOR**: Manage patient files, write prescriptions, view appointments
- **PATIENT**: View medical history, appointments, prescriptions (read-only)
- **PHARMACIST**: Dispense prescriptions, manage inventory
- **ADMIN**: Manage users, create appointments, system configuration
- **DIRECTOR**: Executive dashboard, analytics, reports (read-only)

## Current Status

✅ Phase 0: Project Setup Complete
⏳ Phase 1: Core Infrastructure (In Progress)

## Contributing

Follow the vertical TDD approach and ensure all tests pass before committing.

## License

Private - Hospital Management System
