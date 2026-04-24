package com.hospital.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.management.config.TestSecurityConfig;
import com.hospital.management.dto.DoctorDTO;
import com.hospital.management.repositories.DoctorRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class DoctorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DoctorRepository doctorRepository;

    @BeforeEach
    void setUp() {
        doctorRepository.deleteAll();
    }

    @Test
    void shouldCreateDoctor() throws Exception {
        DoctorDTO dto = createDoctorDTO("Dr. John", "Smith", "john.smith@hospital.com", 
                "+1234567890", "Cardiology", "LIC12345", 10, "MD, FACC");

        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Dr. John"))
                .andExpect(jsonPath("$.specialization").value("Cardiology"))
                .andExpect(jsonPath("$.licenseNumber").value("LIC12345"));
    }

    @Test
    void shouldReturn400WhenInvalidData() throws Exception {
        DoctorDTO dto = new DoctorDTO();
        // Missing required fields

        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void shouldGetDoctorById() throws Exception {
        DoctorDTO created = createAndSaveDoctor("Dr. Jane", "Doe", "jane.doe@hospital.com",
                "+9876543210", "Neurology", "LIC67890", 15, "MD, PhD");

        mockMvc.perform(get("/api/doctors/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.firstName").value("Dr. Jane"))
                .andExpect(jsonPath("$.specialization").value("Neurology"));
    }

    @Test
    void shouldReturn404WhenDoctorNotFound() throws Exception {
        mockMvc.perform(get("/api/doctors/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Doctor not found with id: 999"));
    }

    @Test
    void shouldUpdateDoctor() throws Exception {
        DoctorDTO created = createAndSaveDoctor("Dr. Alice", "Johnson", "alice.johnson@hospital.com",
                "+1111111111", "Pediatrics", "LIC11111", 5, "MD");

        created.setFirstName("Dr. Alicia");
        created.setSpecialization("Pediatric Surgery");
        created.setYearsOfExperience(7);

        mockMvc.perform(put("/api/doctors/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Dr. Alicia"))
                .andExpect(jsonPath("$.specialization").value("Pediatric Surgery"))
                .andExpect(jsonPath("$.yearsOfExperience").value(7));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentDoctor() throws Exception {
        DoctorDTO dto = createDoctorDTO("Dr. Test", "User", "test@hospital.com",
                "+1234567890", "General", "LIC99999", 1, "MD");

        mockMvc.perform(put("/api/doctors/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteDoctor() throws Exception {
        DoctorDTO created = createAndSaveDoctor("Dr. Bob", "Wilson", "bob.wilson@hospital.com",
                "+2222222222", "Orthopedics", "LIC22222", 20, "MD, FAAOS");

        mockMvc.perform(delete("/api/doctors/" + created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/doctors/" + created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentDoctor() throws Exception {
        mockMvc.perform(delete("/api/doctors/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllDoctors() throws Exception {
        createAndSaveDoctor("Dr. Doctor1", "One", "doctor1@hospital.com",
                "+3333333333", "Cardiology", "LIC33333", 8, "MD");
        createAndSaveDoctor("Dr. Doctor2", "Two", "doctor2@hospital.com",
                "+4444444444", "Neurology", "LIC44444", 12, "MD, PhD");

        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldSearchDoctorsBySpecialization() throws Exception {
        createAndSaveDoctor("Dr. Cardio1", "One", "cardio1@hospital.com",
                "+5555555555", "Cardiology", "LIC55555", 10, "MD");
        createAndSaveDoctor("Dr. Cardio2", "Two", "cardio2@hospital.com",
                "+6666666666", "Cardiology", "LIC66666", 15, "MD");
        createAndSaveDoctor("Dr. Neuro", "Three", "neuro@hospital.com",
                "+7777777777", "Neurology", "LIC77777", 8, "MD");

        mockMvc.perform(get("/api/doctors/search")
                        .param("specialization", "Cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].specialization").value("Cardiology"))
                .andExpect(jsonPath("$[1].specialization").value("Cardiology"));
    }

    // Helper methods
    private DoctorDTO createDoctorDTO(String firstName, String lastName, String email,
                                      String phone, String specialization, String licenseNumber,
                                      Integer yearsOfExperience, String qualification) {
        DoctorDTO dto = new DoctorDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        dto.setPhone(phone);
        dto.setSpecialization(specialization);
        dto.setLicenseNumber(licenseNumber);
        dto.setYearsOfExperience(yearsOfExperience);
        dto.setQualification(qualification);
        return dto;
    }

    private DoctorDTO createAndSaveDoctor(String firstName, String lastName, String email,
                                          String phone, String specialization, String licenseNumber,
                                          Integer yearsOfExperience, String qualification) throws Exception {
        DoctorDTO dto = createDoctorDTO(firstName, lastName, email, phone, specialization,
                licenseNumber, yearsOfExperience, qualification);

        String response = mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, DoctorDTO.class);
    }
}
