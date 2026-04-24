package com.hospital.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.management.config.TestSecurityConfig;
import com.hospital.management.dto.PatientDTO;
import com.hospital.management.enums.Gender;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
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
        patientDTO.setPhone("+1234567890");
        patientDTO.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patientDTO.setGender(Gender.MALE);
        patientDTO.setBloodType("O+");
        patientDTO.setAddress("123 Main St");

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

        String response = mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PatientDTO created = objectMapper.readValue(response, PatientDTO.class);

        // When & Then
        mockMvc.perform(get("/api/patients/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    void shouldReturn404WhenPatientNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/patients/999"))
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

        String response = mockMvc.perform(post("/api/patients")
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

        // When & Then
        mockMvc.perform(put("/api/patients/999")
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

        String response = mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PatientDTO created = objectMapper.readValue(response, PatientDTO.class);

        // When & Then - delete the patient
        mockMvc.perform(delete("/api/patients/" + created.getId()))
                .andExpect(status().isNoContent());

        // Verify patient is deleted
        mockMvc.perform(get("/api/patients/" + created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentPatient() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/patients/999"))
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

        PatientDTO patient2 = new PatientDTO();
        patient2.setFirstName("Patient");
        patient2.setLastName("Two");
        patient2.setEmail("patient2@example.com");
        patient2.setPhone("+2222222222");
        patient2.setDateOfBirth(LocalDate.of(1991, 2, 2));
        patient2.setGender(Gender.FEMALE);

        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patient1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patient2)))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
