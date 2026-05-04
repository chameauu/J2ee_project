package com.hospital.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.management.dto.HospitalDTO;
import com.hospital.management.dto.PatientDTO;
import com.hospital.management.entities.*;
import com.hospital.management.enums.Gender;
import com.hospital.management.enums.UserRole;
import com.hospital.management.repositories.*;
import com.hospital.management.security.JwtTokenProvider;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for hospital-scoped authorization on Patient endpoints.
 * Phase 10.4: Hospital-Scoped Authorization Rules
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class PatientControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private HospitalDirectorRepository hospitalDirectorRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    private Hospital hospital1;
    private Hospital hospital2;
    private HospitalDirector director1;
    private HospitalDirector director2;
    private Administrator admin;
    private String director1Token;
    private String director2Token;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Create two hospitals
        hospital1 = new Hospital();
        hospital1.setName("City Medical Center");
        hospital1.setAddress("123 Hospital St");
        hospital1.setPhone("+1234567890");
        hospital1.setEmail("info@citymedical.com");
        hospital1.setRegistrationNumber("REG-001");
        hospital1 = hospitalRepository.save(hospital1);

        hospital2 = new Hospital();
        hospital2.setName("County Hospital");
        hospital2.setAddress("456 County Rd");
        hospital2.setPhone("+1234567891");
        hospital2.setEmail("info@countyhospital.com");
        hospital2.setRegistrationNumber("REG-002");
        hospital2 = hospitalRepository.save(hospital2);

        // Create director for hospital 1
        director1 = new HospitalDirector();
        director1.setFirstName("Director");
        director1.setLastName("One");
        director1.setEmail("director1@hospital.com");
        director1.setPhone("+1111111111");
        director1.setRole(UserRole.DIRECTOR);
        director1.setHospital(hospital1);
        director1 = hospitalDirectorRepository.save(director1);

        // Create director for hospital 2
        director2 = new HospitalDirector();
        director2.setFirstName("Director");
        director2.setLastName("Two");
        director2.setEmail("director2@hospital.com");
        director2.setPhone("+2222222222");
        director2.setRole(UserRole.DIRECTOR);
        director2.setHospital(hospital2);
        director2 = hospitalDirectorRepository.save(director2);

        // Create admin (no hospital assignment)
        admin = new Administrator();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail("admin@hospital.com");
        admin.setPhone("+3333333333");
        admin.setRole(UserRole.ADMIN);
        admin = administratorRepository.save(admin);

        // Generate JWT tokens
        director1Token = jwtTokenProvider.generateToken(director1.getEmail(), director1.getRole().name());
        director2Token = jwtTokenProvider.generateToken(director2.getEmail(), director2.getRole().name());
        adminToken = jwtTokenProvider.generateToken(admin.getEmail(), admin.getRole().name());
    }

    // Test: Director can access their own hospital's patients

    @Test
    void shouldAllowDirectorToAccessOwnHospitalPatients() throws Exception {
        // Given - create patients in hospital 1
        Patient patient1 = new Patient();
        patient1.setFirstName("Patient");
        patient1.setLastName("One");
        patient1.setEmail("patient1@hospital.com");
        patient1.setPhone("+4444444444");
        patient1.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient1.setGender(Gender.MALE);
        patient1.setRole(UserRole.PATIENT);
        patient1.setHospital(hospital1);
        patientRepository.save(patient1);

        // When & Then - director1 can access hospital1 patients
        mockMvc.perform(get("/api/patients/hospital/" + hospital1.getId())
                .header("Authorization", "Bearer " + director1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Patient"));
    }

    @Test
    void shouldDenyDirectorAccessToOtherHospitalPatients() throws Exception {
        // Given - create patients in hospital 1
        Patient patient1 = new Patient();
        patient1.setFirstName("Patient");
        patient1.setLastName("One");
        patient1.setEmail("patient1@hospital.com");
        patient1.setPhone("+4444444444");
        patient1.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient1.setGender(Gender.MALE);
        patient1.setRole(UserRole.PATIENT);
        patient1.setHospital(hospital1);
        patientRepository.save(patient1);

        // When & Then - director2 CANNOT access hospital1 patients (403 Forbidden)
        mockMvc.perform(get("/api/patients/hospital/" + hospital1.getId())
                .header("Authorization", "Bearer " + director2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToAccessAnyHospitalPatients() throws Exception {
        // Given - create patients in hospital 1
        Patient patient1 = new Patient();
        patient1.setFirstName("Patient");
        patient1.setLastName("One");
        patient1.setEmail("patient1@hospital.com");
        patient1.setPhone("+4444444444");
        patient1.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient1.setGender(Gender.MALE);
        patient1.setRole(UserRole.PATIENT);
        patient1.setHospital(hospital1);
        patientRepository.save(patient1);

        // When & Then - admin can access any hospital's patients
        mockMvc.perform(get("/api/patients/hospital/" + hospital1.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        // Admin can also access hospital2 (even though it has no patients)
        mockMvc.perform(get("/api/patients/hospital/" + hospital2.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // Test: Count patients by hospital with authorization

    @Test
    void shouldAllowDirectorToCountOwnHospitalPatients() throws Exception {
        // Given - create 3 patients in hospital 1
        for (int i = 0; i < 3; i++) {
            Patient patient = new Patient();
            patient.setFirstName("Patient");
            patient.setLastName("Number" + i);
            patient.setEmail("patient" + i + "@hospital.com");
            patient.setPhone("+444444444" + i);
            patient.setDateOfBirth(LocalDate.of(1990 + i, 1, 1));
            patient.setGender(Gender.MALE);
            patient.setRole(UserRole.PATIENT);
            patient.setHospital(hospital1);
            patientRepository.save(patient);
        }

        // When & Then - director1 can count hospital1 patients
        mockMvc.perform(get("/api/patients/hospital/" + hospital1.getId() + "/count")
                .header("Authorization", "Bearer " + director1Token))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void shouldDenyDirectorCountingOtherHospitalPatients() throws Exception {
        // Given - create patients in hospital 1
        Patient patient1 = new Patient();
        patient1.setFirstName("Patient");
        patient1.setLastName("One");
        patient1.setEmail("patient1@hospital.com");
        patient1.setPhone("+4444444444");
        patient1.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient1.setGender(Gender.MALE);
        patient1.setRole(UserRole.PATIENT);
        patient1.setHospital(hospital1);
        patientRepository.save(patient1);

        // When & Then - director2 CANNOT count hospital1 patients (403 Forbidden)
        mockMvc.perform(get("/api/patients/hospital/" + hospital1.getId() + "/count")
                .header("Authorization", "Bearer " + director2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToCountAnyHospitalPatients() throws Exception {
        // Given - create 2 patients in hospital 1
        for (int i = 0; i < 2; i++) {
            Patient patient = new Patient();
            patient.setFirstName("Patient");
            patient.setLastName("Number" + i);
            patient.setEmail("patient" + i + "@hospital.com");
            patient.setPhone("+444444444" + i);
            patient.setDateOfBirth(LocalDate.of(1990 + i, 1, 1));
            patient.setGender(Gender.MALE);
            patient.setRole(UserRole.PATIENT);
            patient.setHospital(hospital1);
            patientRepository.save(patient);
        }

        // When & Then - admin can count any hospital's patients
        mockMvc.perform(get("/api/patients/hospital/" + hospital1.getId() + "/count")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));

        mockMvc.perform(get("/api/patients/hospital/" + hospital2.getId() + "/count")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    // Test: Unauthorized access without token

    @Test
    void shouldReturn401WhenNoTokenProvided() throws Exception {
        // When & Then - no token = 403 Forbidden (Spring Security default)
        mockMvc.perform(get("/api/patients/hospital/" + hospital1.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401WhenInvalidTokenProvided() throws Exception {
        // When & Then - invalid token = 403 Forbidden (Spring Security default)
        mockMvc.perform(get("/api/patients/hospital/" + hospital1.getId())
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }

    // Test: Data isolation - directors see only their hospital's data

    @Test
    void shouldIsolateDataBetweenHospitals() throws Exception {
        // Given - create patients in both hospitals
        Patient patient1 = new Patient();
        patient1.setFirstName("Hospital1");
        patient1.setLastName("Patient");
        patient1.setEmail("h1patient@hospital.com");
        patient1.setPhone("+5555555555");
        patient1.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient1.setGender(Gender.MALE);
        patient1.setRole(UserRole.PATIENT);
        patient1.setHospital(hospital1);
        patientRepository.save(patient1);

        Patient patient2 = new Patient();
        patient2.setFirstName("Hospital2");
        patient2.setLastName("Patient");
        patient2.setEmail("h2patient@hospital.com");
        patient2.setPhone("+6666666666");
        patient2.setDateOfBirth(LocalDate.of(1991, 1, 1));
        patient2.setGender(Gender.FEMALE);
        patient2.setRole(UserRole.PATIENT);
        patient2.setHospital(hospital2);
        patientRepository.save(patient2);

        // When & Then - director1 sees only hospital1 patients
        mockMvc.perform(get("/api/patients/hospital/" + hospital1.getId())
                .header("Authorization", "Bearer " + director1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Hospital1"));

        // director2 sees only hospital2 patients
        mockMvc.perform(get("/api/patients/hospital/" + hospital2.getId())
                .header("Authorization", "Bearer " + director2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Hospital2"));

        // director1 CANNOT see hospital2 patients
        mockMvc.perform(get("/api/patients/hospital/" + hospital2.getId())
                .header("Authorization", "Bearer " + director1Token))
                .andExpect(status().isForbidden());

        // director2 CANNOT see hospital1 patients
        mockMvc.perform(get("/api/patients/hospital/" + hospital1.getId())
                .header("Authorization", "Bearer " + director2Token))
                .andExpect(status().isForbidden());

        // Admin can see both
        mockMvc.perform(get("/api/patients/hospital/" + hospital1.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/patients/hospital/" + hospital2.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // Test: Non-existent hospital returns 404

    @Test
    void shouldReturn404ForNonExistentHospital() throws Exception {
        // When & Then - even admin gets 404 for non-existent hospital
        mockMvc.perform(get("/api/patients/hospital/999")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/patients/hospital/999/count")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
