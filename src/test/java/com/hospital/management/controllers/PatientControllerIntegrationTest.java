package com.hospital.management.controllers;

import com.hospital.management.dto.PatientDTO;
import com.hospital.management.entities.Administrator;
import com.hospital.management.entities.Hospital;
import com.hospital.management.enums.Gender;
import com.hospital.management.enums.UserRole;
import com.hospital.management.repositories.AdministratorRepository;
import com.hospital.management.repositories.HospitalRepository;
import com.hospital.management.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PatientControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    private Hospital testHospital;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Clean up
        patientRepository.deleteAll();
        administratorRepository.deleteAll();
        hospitalRepository.deleteAll();
        
        // Create test hospital
        testHospital = testAuthUtils.createTestHospital("Test Hospital");
        
        // Create admin user and generate token
        Administrator admin = testAuthUtils.createTestAdmin("admin@example.com");
        adminToken = testAuthUtils.generateToken("admin@example.com", UserRole.ADMIN);
    }

    @Test
    void shouldCreatePatient() throws Exception {
        // Given
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setFirstName("John");
        patientDTO.setLastName("Doe");
        patientDTO.setEmail("john.doe@example.com");
        patientDTO.setPhone("+1234567890");
        patientDTO.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patientDTO.setGender(Gender.MALE);
        patientDTO.setBloodType("O+");
        patientDTO.setAddress("123 Main St");
        patientDTO.setHospitalId(testHospital.getId());

        // When & Then
        mockMvc.perform(post("/api/patients")
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
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
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetPatientById() throws Exception {
        // Given - create a patient first
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setFirstName("Jane");
        patientDTO.setLastName("Smith");
        patientDTO.setEmail("jane.smith@example.com");
        patientDTO.setPhone("+1234567891");
        patientDTO.setDateOfBirth(LocalDate.of(1985, 5, 15));
        patientDTO.setGender(Gender.FEMALE);
        patientDTO.setHospitalId(testHospital.getId());

        String response = mockMvc.perform(post("/api/patients")
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PatientDTO created = objectMapper.readValue(response, PatientDTO.class);

        // When & Then
        mockMvc.perform(get("/api/patients/" + created.getId())
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    void shouldReturn404WhenPatientNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/patients/999")
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdatePatient() throws Exception {
        // Given - create a patient first
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setFirstName("Alice");
        patientDTO.setLastName("Johnson");
        patientDTO.setEmail("alice.johnson@example.com");
        patientDTO.setPhone("+1234567892");
        patientDTO.setDateOfBirth(LocalDate.of(1992, 3, 20));
        patientDTO.setGender(Gender.FEMALE);
        patientDTO.setHospitalId(testHospital.getId());

        String response = mockMvc.perform(post("/api/patients")
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PatientDTO created = objectMapper.readValue(response, PatientDTO.class);

        // Update the patient
        created.setFirstName("Alicia");
        created.setLastName("Johnson-Smith");
        created.setPhone("+9876543210");

        // When & Then
        mockMvc.perform(put("/api/patients/" + created.getId())
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.firstName").value("Alicia"))
                .andExpect(jsonPath("$.lastName").value("Johnson-Smith"))
                .andExpect(jsonPath("$.phone").value("+9876543210"));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentPatient() throws Exception {
        // Given
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setFirstName("John");
        patientDTO.setLastName("Doe");
        patientDTO.setEmail("john@example.com");
        patientDTO.setPhone("+1234567890");
        patientDTO.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patientDTO.setGender(Gender.MALE);
        patientDTO.setHospitalId(testHospital.getId());

        // When & Then
        mockMvc.perform(put("/api/patients/999")
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePatient() throws Exception {
        // Given - create a patient first
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setFirstName("Bob");
        patientDTO.setLastName("Wilson");
        patientDTO.setEmail("bob.wilson@example.com");
        patientDTO.setPhone("+1234567893");
        patientDTO.setDateOfBirth(LocalDate.of(1988, 7, 10));
        patientDTO.setGender(Gender.MALE);
        patientDTO.setHospitalId(testHospital.getId());

        String response = mockMvc.perform(post("/api/patients")
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PatientDTO created = objectMapper.readValue(response, PatientDTO.class);

        // When & Then - delete the patient
        mockMvc.perform(delete("/api/patients/" + created.getId())
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNoContent());

        // Verify patient is deleted
        mockMvc.perform(get("/api/patients/" + created.getId())
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentPatient() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/patients/999")
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllPatients() throws Exception {
        // Given - create multiple patients
        PatientDTO patient1 = new PatientDTO();
        patient1.setFirstName("Patient");
        patient1.setLastName("One");
        patient1.setEmail("patient1@example.com");
        patient1.setPhone("+1111111111");
        patient1.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient1.setGender(Gender.MALE);
        patient1.setHospitalId(testHospital.getId());

        PatientDTO patient2 = new PatientDTO();
        patient2.setFirstName("Patient");
        patient2.setLastName("Two");
        patient2.setEmail("patient2@example.com");
        patient2.setPhone("+2222222222");
        patient2.setDateOfBirth(LocalDate.of(1991, 2, 2));
        patient2.setGender(Gender.FEMALE);
        patient2.setHospitalId(testHospital.getId());

        mockMvc.perform(post("/api/patients")
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patient1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/patients")
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patient2)))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/api/patients")
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // Phase 10.3: Hospital-scoped endpoint tests
    @Test
    void shouldGetPatientsByHospital() throws Exception {
        // Given - create patients with hospital assignment
        PatientDTO patient1 = new PatientDTO();
        patient1.setFirstName("Hospital");
        patient1.setLastName("Patient1");
        patient1.setEmail("hospital.patient1@example.com");
        patient1.setPhone("+3333333333");
        patient1.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient1.setGender(Gender.MALE);
        patient1.setHospitalId(testHospital.getId());

        PatientDTO patient2 = new PatientDTO();
        patient2.setFirstName("Hospital");
        patient2.setLastName("Patient2");
        patient2.setEmail("hospital.patient2@example.com");
        patient2.setPhone("+4444444444");
        patient2.setDateOfBirth(LocalDate.of(1991, 2, 2));
        patient2.setGender(Gender.FEMALE);
        patient2.setHospitalId(testHospital.getId());

        mockMvc.perform(post("/api/patients")
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patient1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/patients")
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patient2)))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/api/patients/hospital/" + testHospital.getId())
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].hospitalId").value(testHospital.getId()))
                .andExpect(jsonPath("$[1].hospitalId").value(testHospital.getId()));
    }

    @Test
    void shouldReturn404WhenGettingPatientsByNonExistentHospital() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/patients/hospital/999")
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCountPatientsByHospital() throws Exception {
        // Given - create 3 patients with hospital assignment
        for (int i = 0; i < 3; i++) {
            PatientDTO patient = new PatientDTO();
            patient.setFirstName("Count");
            patient.setLastName("Patient" + i);
            patient.setEmail("count.patient" + i + "@example.com");
            patient.setPhone("+555555555" + i);
            patient.setDateOfBirth(LocalDate.of(1990 + i, 1, 1));
            patient.setGender(Gender.MALE);
            patient.setHospitalId(testHospital.getId());

            mockMvc.perform(post("/api/patients")
                    .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(patient)))
                    .andExpect(status().isCreated());
        }

        // When & Then
        mockMvc.perform(get("/api/patients/hospital/" + testHospital.getId() + "/count")
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void shouldReturn404WhenCountingPatientsByNonExistentHospital() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/patients/hospital/999/count")
                .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }
}
