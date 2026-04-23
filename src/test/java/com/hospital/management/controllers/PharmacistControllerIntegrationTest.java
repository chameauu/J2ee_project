package com.hospital.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.management.dto.PharmacistDTO;
import com.hospital.management.repositories.PharmacistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class PharmacistControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PharmacistRepository pharmacistRepository;

    @BeforeEach
    void setUp() {
        pharmacistRepository.deleteAll();
    }

    @Test
    void shouldCreatePharmacist() throws Exception {
        PharmacistDTO dto = createPharmacistDTO("John", "Smith", "john.smith@pharmacy.com",
                "+1234567890", "PLIC12345", "PharmD");

        mockMvc.perform(post("/api/pharmacists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.licenseNumber").value("PLIC12345"));
    }

    @Test
    void shouldReturn400WhenInvalidData() throws Exception {
        PharmacistDTO dto = new PharmacistDTO();
        // Missing required fields

        mockMvc.perform(post("/api/pharmacists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void shouldGetPharmacistById() throws Exception {
        PharmacistDTO created = createAndSavePharmacist("Jane", "Doe", "jane.doe@pharmacy.com",
                "+9876543210", "PLIC67890", "PharmD, RPh");

        mockMvc.perform(get("/api/pharmacists/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void shouldReturn404WhenPharmacistNotFound() throws Exception {
        mockMvc.perform(get("/api/pharmacists/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pharmacist not found with id: 999"));
    }

    @Test
    void shouldUpdatePharmacist() throws Exception {
        PharmacistDTO created = createAndSavePharmacist("Alice", "Johnson", "alice.johnson@pharmacy.com",
                "+1111111111", "PLIC11111", "PharmD");

        created.setFirstName("Alicia");
        created.setQualification("PharmD, BCPS");

        mockMvc.perform(put("/api/pharmacists/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alicia"))
                .andExpect(jsonPath("$.qualification").value("PharmD, BCPS"));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentPharmacist() throws Exception {
        PharmacistDTO dto = createPharmacistDTO("Test", "User", "test@pharmacy.com",
                "+1234567890", "PLIC99999", "PharmD");

        mockMvc.perform(put("/api/pharmacists/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePharmacist() throws Exception {
        PharmacistDTO created = createAndSavePharmacist("Bob", "Wilson", "bob.wilson@pharmacy.com",
                "+2222222222", "PLIC22222", "PharmD");

        mockMvc.perform(delete("/api/pharmacists/" + created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/pharmacists/" + created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentPharmacist() throws Exception {
        mockMvc.perform(delete("/api/pharmacists/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllPharmacists() throws Exception {
        createAndSavePharmacist("Pharmacist1", "One", "pharmacist1@pharmacy.com",
                "+3333333333", "PLIC33333", "PharmD");
        createAndSavePharmacist("Pharmacist2", "Two", "pharmacist2@pharmacy.com",
                "+4444444444", "PLIC44444", "PharmD");

        mockMvc.perform(get("/api/pharmacists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // Helper methods
    private PharmacistDTO createPharmacistDTO(String firstName, String lastName, String email,
                                              String phone, String licenseNumber, String qualification) {
        PharmacistDTO dto = new PharmacistDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        dto.setPhone(phone);
        dto.setLicenseNumber(licenseNumber);
        dto.setQualification(qualification);
        return dto;
    }

    private PharmacistDTO createAndSavePharmacist(String firstName, String lastName, String email,
                                                  String phone, String licenseNumber, String qualification) throws Exception {
        PharmacistDTO dto = createPharmacistDTO(firstName, lastName, email, phone, licenseNumber, qualification);

        String response = mockMvc.perform(post("/api/pharmacists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, PharmacistDTO.class);
    }
}
