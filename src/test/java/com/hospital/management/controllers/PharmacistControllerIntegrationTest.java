package com.hospital.management.controllers;

import com.hospital.management.dto.PharmacistDTO;
import com.hospital.management.entities.Administrator;
import com.hospital.management.entities.Hospital;
import com.hospital.management.enums.UserRole;
import com.hospital.management.repositories.AdministratorRepository;
import com.hospital.management.repositories.HospitalRepository;
import com.hospital.management.repositories.PharmacistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PharmacistControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PharmacistRepository pharmacistRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    private Hospital testHospital;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Clean up
        pharmacistRepository.deleteAll();
        administratorRepository.deleteAll();
        hospitalRepository.deleteAll();
        
        // Create test hospital
        testHospital = testAuthUtils.createTestHospital("Test Hospital");
        
        // Create admin user and generate token
        testAuthUtils.createTestAdmin("admin@example.com");
        adminToken = testAuthUtils.generateToken("admin@example.com", UserRole.ADMIN);
    }

    @Test
    void shouldCreatePharmacist() throws Exception {
        PharmacistDTO dto = createPharmacistDTO("John", "Smith", "john.smith@pharmacy.com",
                "+1234567890", "PLIC12345", "PharmD");

        mockMvc.perform(post("/api/pharmacists")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
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
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void shouldGetPharmacistById() throws Exception {
        PharmacistDTO created = createAndSavePharmacist("Jane", "Doe", "jane.doe@pharmacy.com",
                "+9876543210", "PLIC67890", "PharmD, RPh");

        mockMvc.perform(get("/api/pharmacists/" + created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void shouldReturn404WhenPharmacistNotFound() throws Exception {
        mockMvc.perform(get("/api/pharmacists/999")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
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
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
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
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePharmacist() throws Exception {
        PharmacistDTO created = createAndSavePharmacist("Bob", "Wilson", "bob.wilson@pharmacy.com",
                "+2222222222", "PLIC22222", "PharmD");

        mockMvc.perform(delete("/api/pharmacists/" + created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/pharmacists/" + created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentPharmacist() throws Exception {
        mockMvc.perform(delete("/api/pharmacists/999")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllPharmacists() throws Exception {
        createAndSavePharmacist("Pharmacist1", "One", "pharmacist1@pharmacy.com",
                "+3333333333", "PLIC33333", "PharmD");
        createAndSavePharmacist("Pharmacist2", "Two", "pharmacist2@pharmacy.com",
                "+4444444444", "PLIC44444", "PharmD");

        mockMvc.perform(get("/api/pharmacists")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
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
        dto.setHospitalId(testHospital.getId());
        return dto;
    }

    private PharmacistDTO createAndSavePharmacist(String firstName, String lastName, String email,
                                                  String phone, String licenseNumber, String qualification) throws Exception {
        PharmacistDTO dto = createPharmacistDTO(firstName, lastName, email, phone, licenseNumber, qualification);

        String response = mockMvc.perform(post("/api/pharmacists")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, PharmacistDTO.class);
    }
}
