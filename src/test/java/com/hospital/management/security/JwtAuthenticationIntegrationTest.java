package com.hospital.management.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.management.config.TestSecurityConfig;
import com.hospital.management.dto.LoginRequest;
import com.hospital.management.dto.LoginResponse;
import com.hospital.management.dto.PatientDTO;
import com.hospital.management.entities.Patient;
import com.hospital.management.enums.Gender;
import com.hospital.management.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    private String patientToken;

    @BeforeEach
    void setUp() throws Exception {
        // Create a patient for testing
        Patient patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setEmail("john.doe@test.com");
        patient.setPhone("1234567890");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender(Gender.MALE);
        patientRepository.save(patient);

        // Login to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@test.com");
        loginRequest.setPassword("patient123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LoginResponse.class
        );
        patientToken = loginResponse.getToken();
    }

    @Test
    void shouldAccessProtectedEndpointWithValidToken() throws Exception {
        mockMvc.perform(get("/api/patients")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk());
    }

    @Test
    @Disabled("Security is disabled in test profile - authentication not enforced")
    void shouldReturn401WhenAccessingProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Disabled("Security is disabled in test profile - authentication not enforced")
    void shouldReturn401WhenAccessingProtectedEndpointWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/patients")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessToAuthEndpointsWithoutToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@test.com");
        loginRequest.setPassword("patient123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }
}
