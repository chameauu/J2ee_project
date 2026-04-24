package com.hospital.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.management.config.TestSecurityConfig;
import com.hospital.management.dto.DoctorDTO;
import com.hospital.management.dto.LoginRequest;
import com.hospital.management.dto.PatientDTO;
import com.hospital.management.enums.Gender;
import com.hospital.management.repositories.DoctorRepository;
import com.hospital.management.repositories.PatientRepository;
import com.hospital.management.repositories.PharmacistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PharmacistRepository pharmacistRepository;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
        pharmacistRepository.deleteAll();
    }

    @Test
    void shouldLoginWithValidPatientCredentials() throws Exception {
        // Create a patient first
        createPatient("patient@example.com");

        LoginRequest loginRequest = new LoginRequest("patient@example.com", "patient123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("patient@example.com"))
                .andExpect(jsonPath("$.role").value("PATIENT"))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void shouldLoginWithValidDoctorCredentials() throws Exception {
        // Create a doctor first
        createDoctor("doctor@example.com");

        LoginRequest loginRequest = new LoginRequest("doctor@example.com", "doctor123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("doctor@example.com"))
                .andExpect(jsonPath("$.role").value("DOCTOR"));
    }

    @Test
    void shouldReturn401WithInvalidPassword() throws Exception {
        // Create a patient first
        createPatient("patient@example.com");

        LoginRequest loginRequest = new LoginRequest("patient@example.com", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void shouldReturn401WithNonExistentEmail() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void shouldReturn400WithInvalidEmailFormat() throws Exception {
        LoginRequest loginRequest = new LoginRequest("invalid-email", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Invalid email format"));
    }

    @Test
    void shouldReturn400WithMissingPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password is required"));
    }

    // Helper methods
    private void createPatient(String email) throws Exception {
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("Test");
        dto.setLastName("Patient");
        dto.setEmail(email);
        dto.setPhone("+1234567890");
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        dto.setGender(Gender.MALE);

        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    private void createDoctor(String email) throws Exception {
        DoctorDTO dto = new DoctorDTO();
        dto.setFirstName("Test");
        dto.setLastName("Doctor");
        dto.setEmail(email);
        dto.setPhone("+1234567890");
        dto.setSpecialization("Cardiology");
        dto.setLicenseNumber("LIC" + System.currentTimeMillis());
        dto.setYearsOfExperience(10);
        dto.setQualification("MD");

        mockMvc.perform(post("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }
}
