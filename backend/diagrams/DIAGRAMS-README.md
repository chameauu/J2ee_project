# Hospital Management System - UML Diagrams

This folder contains comprehensive UML diagrams for the Hospital Management System using PlantUML format.

## Diagram Files

### 1. `hospital-class-diagram.puml`
**Complete Class Diagram with All Details**

Contains:
- All entities with full attributes and methods
- All enumerations (UserRole, Gender, AppointmentStatus, etc.)
- Complete relationship mappings
- Inheritance hierarchy
- Multiplicity indicators
- Detailed notes

**Best for:** Understanding the complete domain model and all entity relationships.

### 2. `hospital-class-diagram-simplified.puml`
**Simplified Class Diagram**

Contains:
- Core entities with key attributes only
- Main relationships
- Simplified view for quick understanding
- Key notes highlighting important concepts

**Best for:** Getting a quick overview of the system structure without overwhelming details.

### 3. `hospital-modules-diagram.puml`
**Module-Based Organization**

Contains:
- Entities grouped by functional modules:
  - Doctor Module
  - Patient Module
  - Appointment Module
  - Medical Records Module
  - Prescription Module
  - Pharmacy Module
  - Admin Module
  - Hospital Director Module
- Cross-module relationships
- Module-specific methods and responsibilities

**Best for:** Understanding how the system is organized into functional modules and how they interact.

### 4. `hospital-architecture-layers.puml`
**4-Layer Architecture Diagram**

Contains:
- Controller Layer (REST endpoints)
- Service Layer (business logic)
- Repository Layer (data access)
- Entity Layer (domain model)
- Cross-cutting concerns (security, multi-tenancy)
- Layer dependencies and interactions

**Best for:** Understanding the architectural layers and how they communicate following Spring Boot best practices.

### 5. `hospital-sequence-diagrams.puml`
**Sequence Diagrams for Key Workflows**

Contains 7 sequence diagrams:
1. Doctor views patient medical file
2. Doctor updates medical record with notes
3. Administrator creates appointment for patient
4. Doctor writes prescription
5. Pharmacist dispenses prescription
6. Admin views dashboard statistics
7. User authentication flow (JWT)
8. Hospital Director views executive dashboard
9. Hospital Director analyzes doctor performance

**Best for:** Understanding the flow of operations and interactions between components for specific use cases.

---

## How to View the Diagrams

### Option 1: Online PlantUML Editor
1. Go to http://www.plantuml.com/plantuml/uml/
2. Copy the content of any `.puml` file
3. Paste it into the editor
4. View the generated diagram

### Option 2: VS Code Extension
1. Install "PlantUML" extension in VS Code
2. Open any `.puml` file
3. Press `Alt+D` to preview the diagram
4. Or right-click and select "Preview Current Diagram"

### Option 3: IntelliJ IDEA Plugin
1. Install "PlantUML integration" plugin
2. Open any `.puml` file
3. The diagram will render automatically in the editor

### Option 4: Command Line (requires PlantUML installed)
```bash
# Install PlantUML (requires Java)
# On macOS with Homebrew:
brew install plantuml

# Generate PNG image
plantuml hospital-class-diagram.puml

# Generate SVG image
plantuml -tsvg hospital-class-diagram.puml

# Generate all diagrams
plantuml *.puml
```

### Option 5: Docker
```bash
# Generate PNG images using Docker
docker run --rm -v $(pwd):/data plantuml/plantuml *.puml

# Generate SVG images
docker run --rm -v $(pwd):/data plantuml/plantuml -tsvg *.puml
```

---

## Diagram Legend

### Colors
- **Green (#E8F5E9)**: Entity classes
- **Orange (#FFF3E0)**: Abstract classes
- **Blue (#E3F2FD)**: Enumerations
- **Different colors in module diagram**: Different functional modules

### Relationship Symbols
- `--|>` : Inheritance (extends)
- `--` : Association
- `*--` : Composition (strong ownership)
- `o--` : Aggregation (weak ownership)
- `..>` : Dependency (uses)
- `..|>` : Interface implementation

### Multiplicity
- `1` : Exactly one
- `0..1` : Zero or one
- `0..*` : Zero or many
- `1..*` : One or many

### Stereotypes
- `<<@Entity>>` : JPA Entity
- `<<@RestController>>` : Spring REST Controller
- `<<@Service>>` : Spring Service
- `<<@Repository>>` : Spring Repository
- `<<interface>>` : Java Interface
- `<<enumeration>>` : Java Enum

---

## Key Design Patterns Illustrated

### 1. Inheritance (User Hierarchy)
```
User (abstract)
├── Doctor
├── Patient
├── Pharmacist
├── Administrator
└── HospitalDirector
```

### 2. Composition
- `Prescription` *-- `PrescriptionItem` (Prescription owns items)
- `MedicalRecord` *-- `MedicalNote` (Record owns notes)

### 3. Association
- `Doctor` -- `Patient` (Many-to-Many through appointments)
- `Patient` -- `MedicalRecord` (One-to-Many)

### 4. Layered Architecture
```
Controllers → Services → Repositories → Entities
```

### 5. Repository Pattern
- All repositories extend `JpaRepository`
- Custom query methods defined in interfaces
- Spring Data JPA generates implementations

### 6. Service Layer Pattern
- Interface + Implementation
- Business logic encapsulation
- Transaction management

---

## Understanding the Relationships

### Doctor-Patient Relationship
- **Direct**: Through `MedicalRecord` (Doctor creates records for Patient)
- **Indirect**: Through `Appointment` (Doctor schedules appointments with Patient)
- **Indirect**: Through `Prescription` (Doctor writes prescriptions for Patient)

### Prescription Flow
1. Doctor writes `Prescription` for Patient
2. `Prescription` contains multiple `PrescriptionItem`
3. Each `PrescriptionItem` references a `Medication`
4. Pharmacist dispenses `PrescriptionItem`
5. `PharmacyStock` is reduced

### Medical Record Structure
```
MedicalRecord
├── Contains: MedicalNote (0..*)
├── Generates: Prescription (0..*)
├── Includes: LabResult (0..*)
├── Belongs to: Patient (1)
└── Created by: Doctor (1)
```

---

## Mapping to Database Tables

### User Tables (Joined Table Inheritance)
```
users (base table)
├── doctors (extends users)
├── patients (extends users)
├── pharmacists (extends users)
├── administrators (extends users)
└── hospital_directors (extends users)
```

### Many-to-Many Join Tables
```
student_courses (for Student ↔ Course)
├── student_id (FK to students)
└── courses_id (FK to courses)
```

### One-to-Many Foreign Keys
```
medical_records
├── patient_id (FK to patients)
└── doctor_id (FK to doctors)

appointments
├── patient_id (FK to patients)
└── doctor_id (FK to doctors)
```

---

## Use Cases Covered

### Doctor Use Cases
✅ View patient list
✅ View patient medical file
✅ Add medical notes
✅ View schedule
✅ Write prescriptions
✅ View statistics

### Patient Use Cases
✅ View medical history
✅ Read doctor notes
✅ Book appointments
✅ View prescriptions
✅ View lab results

### Pharmacist Use Cases
✅ View prescriptions
✅ Dispense medications
✅ Manage stock
✅ Check inventory

### Administrator Use Cases
✅ View dashboard
✅ Manage users
✅ Generate reports
✅ View system statistics

### Hospital Director Use Cases
✅ View executive dashboard with KPIs
✅ Monitor all doctors and performance metrics
✅ View all patients and demographics
✅ Monitor pharmacy inventory and stock levels
✅ Access comprehensive analytics
✅ Generate executive reports

---

## Integration with Code

These diagrams directly map to the code structure:

### Entities → Java Classes
```java
@Entity
public class Patient extends User {
    // Matches Patient class in diagram
}
```

### Repositories → Interfaces
```java
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    // Matches PatientRepository in diagram
}
```

### Services → Implementation
```java
@Service
public class PatientServiceImpl implements IPatientService {
    // Matches PatientServiceImpl in diagram
}
```

### Controllers → REST Endpoints
```java
@RestController
@RequestMapping("/api/patients")
public class PatientController {
    // Matches PatientController in diagram
}
```

---

## Extending the Diagrams

To add new features:

1. **Add Entity**: Update `hospital-class-diagram.puml`
2. **Add Module**: Update `hospital-modules-diagram.puml`
3. **Add Layer**: Update `hospital-architecture-layers.puml`
4. **Add Workflow**: Add sequence diagram to `hospital-sequence-diagrams.puml`

---

## Tips for Reading the Diagrams

1. **Start with simplified diagram** to get the big picture
2. **Move to module diagram** to understand functional organization
3. **Review architecture diagram** to see layer interactions
4. **Study sequence diagrams** for specific workflows
5. **Refer to complete diagram** for detailed relationships

---

## Exporting Diagrams

### For Documentation
```bash
# Generate high-quality PNG
plantuml -tpng hospital-class-diagram.puml

# Generate scalable SVG
plantuml -tsvg hospital-class-diagram.puml

# Generate PDF
plantuml -tpdf hospital-class-diagram.puml
```

### For Presentations
- Use SVG format for best quality
- Export individual modules for focused presentations
- Use sequence diagrams to explain workflows

---

## Questions?

These diagrams follow:
- UML 2.5 standard
- Spring Boot best practices
- JPA/Hibernate conventions
- RESTful API design principles

Refer to `hospital-management-system-spec.md` for detailed implementation specifications.
