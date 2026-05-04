package com.hospital.management.integration;

import com.hospital.management.dto.*;
import com.hospital.management.entities.*;
import com.hospital.management.enums.*;
import com.hospital.management.repositories.*;
import com.hospital.management.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Data Ownership Integration Tests
 * 
 * Tests that verify data ownership and access control rules:
 * - Doctors see only their patients
 * - Directors see only their hospital staff
 * - Patients see only their own data
 * - Cross-hospital access is denied
 * - Admins see all data
 * 
 * Phase 10.8: Comprehensive Testing
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DataOwnershipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private HospitalDirectorRepository hospitalDirectorRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    private Hospital hospital1;
    private Hospital hospital2;
    private Doctor doctor1;
    private Doctor doctor2;
    private Patient patient1;
    private Patient patient2;
    private HospitalDirector director1;
    private HospitalDirector director2;
    private Administrator admin;
    private MedicalRecord medicalRecord1;
    private MedicalRecord medicalRecord2;
    private Appointment appointment1;
    private Appointment appointment2;
    private Prescription prescription1;
    private Prescription prescription2;

    @BeforeEach
    void setUp() {
        // Create Hospital 1
        hospital1 = new Hospital();
        hospital1.setName("City General Hospital");
        hospital1.setAddress("123 Main St");
        hospital1.setPhone("555-0001");
        hospital1.setEmail("contact@citygeneral.com");
        hospital1.setRegistrationNumber("REG-001");
        hospital1.setEstablishedDate(LocalDate.of(2000, 1, 1));
        hospital1 = hospitalRepository.save(hospital1);

        // Create Hospital 2
        hospital2 = new Hospital();
        hospital2.setName("County Medical Center");
        hospital2.setAddress("456 Oak Ave");
        hospital2.setPhone("555-0002");
        hospital2.setEmail("contact@countymedical.com");
        hospital2.setRegistrationNumber("REG-002");
        hospital2.setEstablishedDate(LocalDate.of(2005, 1, 1));
        hospital2 = hospitalRepository.save(hospital2);

        // Create Doctor 1 (Hospital 1)
        doctor1 = new Doctor();
        doctor1.setFirstName("John");
        doctor1.setLastName("Smith");
        doctor1.setEmail("john.smith@citygeneral.com");
        doctor1.setPhone("555-1001");
        doctor1.setSpecialization("Cardiology");
        doctor1.setLicenseNumber("DOC-001");
        doctor1.setYearsOfExperience(10);
        doctor1.setQualification("MD");
        doctor1.setHospital(hospital1);
        doctor1 = doctorRepository.save(doctor1);

        // Create Doctor 2 (Hospital 2)
        doctor2 = new Doctor();
        doctor2.setFirstName("Jane");
        doctor2.setLastName("Doe");
        doctor2.setEmail("jane.doe@countymedical.com");
        doctor2.setPhone("555-1002");
        doctor2.setSpecialization("Neurology");
        doctor2.setLicenseNumber("DOC-002");
        doctor2.setYearsOfExperience(8);
        doctor2.setQualification("MD");
        doctor2.setHospital(hospital2);
        doctor2 = doctorRepository.save(doctor2);

        // Create Patient 1 (Hospital 1)
        patient1 = new Patient();
        patient1.setFirstName("Alice");
        patient1.setLastName("Johnson");
        patient1.setEmail("alice.johnson@email.com");
        patient1.setPhone("555-2001");
        patient1.setDateOfBirth(LocalDate.of(1990, 5, 15));
        patient1.setGender(Gender.FEMALE);
        patient1.setBloodType("A+");
        patient1.setAddress("789 Elm St");
        patient1.setHospital(hospital1);
        patient1 = patientRepository.save(patient1);

        // Create Patient 2 (Hospital 2)
        patient2 = new Patient();
        patient2.setFirstName("Bob");
        patient2.setLastName("Williams");
        patient2.setEmail("bob.williams@email.com");
        patient2.setPhone("555-2002");
        patient2.setDateOfBirth(LocalDate.of(1985, 8, 20));
        patient2.setGender(Gender.MALE);
        patient2.setBloodType("B+");
        patient2.setAddress("321 Pine St");
        patient2.setHospital(hospital2);
        patient2 = patientRepository.save(patient2);

        // Create Director 1 (Hospital 1)
        director1 = new HospitalDirector();
        director1.setFirstName("Michael");
        director1.setLastName("Brown");
        director1.setEmail("michael.brown@citygeneral.com");
        director1.setPhone("555-3001");
        director1.setHospital(hospital1);
        director1 = hospitalDirectorRepository.save(director1);

        // Create Director 2 (Hospital 2)
        director2 = new HospitalDirector();
        director2.setFirstName("Sarah");
        director2.setLastName("Davis");
        director2.setEmail("sarah.davis@countymedical.com");
        director2.setPhone("555-3002");
        director2.setHospital(hospital2);
        director2 = hospitalDirectorRepository.save(director2);

        // Create Admin (no hospital - system-wide)
        admin = new Administrator();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail("admin@hospital.com");
        admin.setPhone("555-9999");
        admin.setHospital(hospital1); // Admins still need a hospital assignment
        admin = administratorRepository.save(admin);

        // Create Medical Record 1 (Doctor 1, Patient 1, Hospital 1)
        medicalRecord1 = new MedicalRecord();
        medicalRecord1.setDoctor(doctor1);
        medicalRecord1.setPatient(patient1);
        medicalRecord1.setHospital(hospital1);
        medicalRecord1.setVisitDate(LocalDateTime.now());
        medicalRecord1.setChiefComplaint("Chest pain");
        medicalRecord1.setDiagnosis("Angina");
        medicalRecord1.setTreatment("Medication prescribed");
        medicalRecord1.setNotes("Follow up in 2 weeks");
        medicalRecord1 = medicalRecordRepository.save(medicalRecord1);

        // Create Medical Record 2 (Doctor 2, Patient 2, Hospital 2)
        medicalRecord2 = new MedicalRecord();
        medicalRecord2.setDoctor(doctor2);
        medicalRecord2.setPatient(patient2);
        medicalRecord2.setHospital(hospital2);
        medicalRecord2.setVisitDate(LocalDateTime.now());
        medicalRecord2.setChiefComplaint("Headache");
        medicalRecord2.setDiagnosis("Migraine");
        medicalRecord2.setTreatment("Pain relief medication");
        medicalRecord2.setNotes("Monitor symptoms");
        medicalRecord2 = medicalRecordRepository.save(medicalRecord2);

        // Create Appointment 1 (Doctor 1, Patient 1, Hospital 1)
        appointment1 = new Appointment();
        appointment1.setDoctor(doctor1);
        appointment1.setPatient(patient1);
        appointment1.setHospital(hospital1);
        appointment1.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        appointment1.setDurationMinutes(30);
        appointment1.setType(AppointmentType.CONSULTATION);
        appointment1.setStatus(AppointmentStatus.SCHEDULED);
        appointment1.setReason("Follow-up consultation");
        appointment1 = appointmentRepository.save(appointment1);

        // Create Appointment 2 (Doctor 2, Patient 2, Hospital 2)
        appointment2 = new Appointment();
        appointment2.setDoctor(doctor2);
        appointment2.setPatient(patient2);
        appointment2.setHospital(hospital2);
        appointment2.setAppointmentDateTime(LocalDateTime.now().plusDays(2));
        appointment2.setDurationMinutes(45);
        appointment2.setType(AppointmentType.CONSULTATION);
        appointment2.setStatus(AppointmentStatus.SCHEDULED);
        appointment2.setReason("Neurological assessment");
        appointment2 = appointmentRepository.save(appointment2);

        // Create Prescription 1 (Doctor 1, Patient 1, Hospital 1)
        prescription1 = new Prescription();
        prescription1.setDoctor(doctor1);
        prescription1.setPatient(patient1);
        prescription1.setHospital(hospital1);
        prescription1.setMedicalRecord(medicalRecord1);
        prescription1.setPrescribedDate(LocalDateTime.now());
        prescription1.setValidUntil(LocalDate.now().plusMonths(1));
        prescription1.setMedicationName("Aspirin");
        prescription1.setDosage("100mg");
        prescription1.setFrequency("Once daily");
        prescription1.setDurationDays(30);
        prescription1.setInstructions("Take with food");
        prescription1.setStatus(PrescriptionStatus.ACTIVE);
        prescription1 = prescriptionRepository.save(prescription1);

        // Create Prescription 2 (Doctor 2, Patient 2, Hospital 2)
        prescription2 = new Prescription();
        prescription2.setDoctor(doctor2);
        prescription2.setPatient(patient2);
        prescription2.setHospital(hospital2);
        prescription2.setMedicalRecord(medicalRecord2);
        prescription2.setPrescribedDate(LocalDateTime.now());
        prescription2.setValidUntil(LocalDate.now().plusMonths(1));
        prescription2.setMedicationName("Ibuprofen");
        prescription2.setDosage("400mg");
        prescription2.setFrequency("As needed");
        prescription2.setDurationDays(14);
        prescription2.setInstructions("Take for pain relief");
        prescription2.setStatus(PrescriptionStatus.ACTIVE);
        prescription2 = prescriptionRepository.save(prescription2);
    }

    // ==================== Doctor Access Tests ====================

    @Test
    void shouldAllowDoctorToAccessOwnPatientsMedicalRecords() throws Exception {
        String token = jwtTokenProvider.generateToken(doctor1.getEmail(), UserRole.DOCTOR.name());

        mockMvc.perform(get("/api/doctors/" + doctor1.getId() + "/medical-records")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].patientId").value(patient1.getId()));
    }

    @Test
    void shouldDenyDoctorAccessToOtherDoctorsMedicalRecords() throws Exception {
        String token = jwtTokenProvider.generateToken(doctor1.getEmail(), UserRole.DOCTOR.name());

        // Doctor 1 tries to access Doctor 2's medical records
        mockMvc.perform(get("/api/doctors/" + doctor2.getId() + "/medical-records")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowDoctorToAccessOwnAppointments() throws Exception {
        String token = jwtTokenProvider.generateToken(doctor1.getEmail(), UserRole.DOCTOR.name());

        mockMvc.perform(get("/api/appointments/doctor/" + doctor1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].doctorId").value(doctor1.getId()));
    }

    @Test
    void shouldDenyDoctorAccessToOtherDoctorsAppointments() throws Exception {
        String token = jwtTokenProvider.generateToken(doctor1.getEmail(), UserRole.DOCTOR.name());

        // Doctor 1 tries to access Doctor 2's appointments
        mockMvc.perform(get("/api/appointments/doctor/" + doctor2.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowDoctorToAccessOwnPrescriptions() throws Exception {
        String token = jwtTokenProvider.generateToken(doctor1.getEmail(), UserRole.DOCTOR.name());

        mockMvc.perform(get("/api/prescriptions/doctor/" + doctor1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].doctorId").value(doctor1.getId()));
    }

    // ==================== Director Access Tests ====================

    @Test
    void shouldAllowDirectorToAccessOwnHospitalDoctors() throws Exception {
        String token = jwtTokenProvider.generateToken(director1.getEmail(), UserRole.DIRECTOR.name());

        mockMvc.perform(get("/api/hospitals/" + hospital1.getId() + "/doctors")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].hospitalId").value(hospital1.getId()));
    }

    @Test
    void shouldDenyDirectorAccessToOtherHospitalDoctors() throws Exception {
        String token = jwtTokenProvider.generateToken(director1.getEmail(), UserRole.DIRECTOR.name());

        // Director 1 tries to access Hospital 2's doctors
        mockMvc.perform(get("/api/hospitals/" + hospital2.getId() + "/doctors")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowDirectorToAccessOwnHospitalPatients() throws Exception {
        String token = jwtTokenProvider.generateToken(director1.getEmail(), UserRole.DIRECTOR.name());

        mockMvc.perform(get("/api/hospitals/" + hospital1.getId() + "/patients")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].hospitalId").value(hospital1.getId()));
    }

    @Test
    void shouldDenyDirectorAccessToOtherHospitalPatients() throws Exception {
        String token = jwtTokenProvider.generateToken(director1.getEmail(), UserRole.DIRECTOR.name());

        // Director 1 tries to access Hospital 2's patients
        mockMvc.perform(get("/api/hospitals/" + hospital2.getId() + "/patients")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowDirectorToAccessOwnHospitalStatistics() throws Exception {
        String token = jwtTokenProvider.generateToken(director1.getEmail(), UserRole.DIRECTOR.name());

        mockMvc.perform(get("/api/director/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hospitalId").value(hospital1.getId()));
    }

    // ==================== Patient Access Tests ====================

    @Test
    void shouldAllowPatientToAccessOwnMedicalRecords() throws Exception {
        String token = jwtTokenProvider.generateToken(patient1.getEmail(), UserRole.PATIENT.name());

        mockMvc.perform(get("/api/patients/" + patient1.getId() + "/medical-records")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].patientId").value(patient1.getId()));
    }

    @Test
    void shouldDenyPatientAccessToOtherPatientsMedicalRecords() throws Exception {
        String token = jwtTokenProvider.generateToken(patient1.getEmail(), UserRole.PATIENT.name());

        // Patient 1 tries to access Patient 2's medical records
        mockMvc.perform(get("/api/patients/" + patient2.getId() + "/medical-records")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowPatientToAccessOwnAppointments() throws Exception {
        String token = jwtTokenProvider.generateToken(patient1.getEmail(), UserRole.PATIENT.name());

        mockMvc.perform(get("/api/appointments/patient/" + patient1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].patientId").value(patient1.getId()));
    }

    @Test
    void shouldDenyPatientAccessToOtherPatientsAppointments() throws Exception {
        String token = jwtTokenProvider.generateToken(patient1.getEmail(), UserRole.PATIENT.name());

        // Patient 1 tries to access Patient 2's appointments
        mockMvc.perform(get("/api/appointments/patient/" + patient2.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowPatientToAccessOwnPrescriptions() throws Exception {
        String token = jwtTokenProvider.generateToken(patient1.getEmail(), UserRole.PATIENT.name());

        mockMvc.perform(get("/api/prescriptions/patient/" + patient1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].patientId").value(patient1.getId()));
    }

    // ==================== Admin Access Tests ====================

    @Test
    void shouldAllowAdminToAccessAllHospitals() throws Exception {
        String token = jwtTokenProvider.generateToken(admin.getEmail(), UserRole.ADMIN.name());

        mockMvc.perform(get("/api/hospitals")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2)); // Both hospitals
    }

    @Test
    void shouldAllowAdminToAccessAnyHospitalDoctors() throws Exception {
        String token = jwtTokenProvider.generateToken(admin.getEmail(), UserRole.ADMIN.name());

        // Admin accesses Hospital 2's doctors
        mockMvc.perform(get("/api/hospitals/" + hospital2.getId() + "/doctors")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldAllowAdminToAccessSystemWideStatistics() throws Exception {
        String token = jwtTokenProvider.generateToken(admin.getEmail(), UserRole.ADMIN.name());

        mockMvc.perform(get("/api/statistics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDoctors").exists())
                .andExpect(jsonPath("$.totalPatients").exists());
    }
}
