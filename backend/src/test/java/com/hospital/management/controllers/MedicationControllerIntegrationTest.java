package com.hospital.management.controllers;

import com.hospital.management.dto.MedicationDTO;
import com.hospital.management.enums.MedicationType;
import com.hospital.management.enums.UserRole;
import com.hospital.management.repositories.AdministratorRepository;
import com.hospital.management.repositories.MedicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MedicationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    private MedicationDTO medicationDTO;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Clean up
        medicationRepository.deleteAll();
        administratorRepository.deleteAll();
        
        // Create admin user and generate token
        testAuthUtils.createTestAdmin("admin@example.com");
        adminToken = testAuthUtils.generateToken("admin@example.com", UserRole.ADMIN);
        
        // Setup test DTO
        medicationDTO = new MedicationDTO();
        medicationDTO.setName("Aspirin");
        medicationDTO.setGenericName("Acetylsalicylic Acid");
        medicationDTO.setManufacturer("PharmaCorp");
        medicationDTO.setType(MedicationType.TABLET);
        medicationDTO.setStrength("500mg");
        medicationDTO.setDescription("Pain reliever and anti-inflammatory");
    }

    @Test
    void shouldCreateMedication() throws Exception {
        mockMvc.perform(post("/api/pharmacy/medications")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Aspirin")))
                .andExpect(jsonPath("$.genericName", is("Acetylsalicylic Acid")))
                .andExpect(jsonPath("$.manufacturer", is("PharmaCorp")))
                .andExpect(jsonPath("$.type", is("TABLET")))
                .andExpect(jsonPath("$.strength", is("500mg")));
    }

    @Test
    void shouldReturn400WhenInvalidData() throws Exception {
        medicationDTO.setName(null);

        mockMvc.perform(post("/api/pharmacy/medications")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicationDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetMedicationById() throws Exception {
        String response = mockMvc.perform(post("/api/pharmacy/medications")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicationDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        MedicationDTO created = objectMapper.readValue(response, MedicationDTO.class);

        mockMvc.perform(get("/api/pharmacy/medications/{id}", created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(created.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Aspirin")));
    }

    @Test
    void shouldReturn404WhenMedicationNotFound() throws Exception {
        mockMvc.perform(get("/api/pharmacy/medications/{id}", 999L)
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateMedication() throws Exception {
        String response = mockMvc.perform(post("/api/pharmacy/medications")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicationDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        MedicationDTO created = objectMapper.readValue(response, MedicationDTO.class);
        created.setStrength("1000mg");
        created.setDescription("Updated description");

        mockMvc.perform(put("/api/pharmacy/medications/{id}", created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.strength", is("1000mg")))
                .andExpect(jsonPath("$.description", is("Updated description")));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentMedication() throws Exception {
        mockMvc.perform(put("/api/pharmacy/medications/{id}", 999L)
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicationDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteMedication() throws Exception {
        String response = mockMvc.perform(post("/api/pharmacy/medications")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicationDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        MedicationDTO created = objectMapper.readValue(response, MedicationDTO.class);

        mockMvc.perform(delete("/api/pharmacy/medications/{id}", created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/pharmacy/medications/{id}", created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentMedication() throws Exception {
        mockMvc.perform(delete("/api/pharmacy/medications/{id}", 999L)
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllMedications() throws Exception {
        mockMvc.perform(post("/api/pharmacy/medications")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicationDTO)))
                .andExpect(status().isCreated());

        MedicationDTO medication2 = new MedicationDTO();
        medication2.setName("Ibuprofen");
        medication2.setGenericName("Ibuprofen");
        medication2.setManufacturer("MediCorp");
        medication2.setType(MedicationType.CAPSULE);
        medication2.setStrength("200mg");
        medication2.setDescription("Pain reliever");

        mockMvc.perform(post("/api/pharmacy/medications")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medication2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/pharmacy/medications")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldSearchMedications() throws Exception {
        mockMvc.perform(post("/api/pharmacy/medications")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicationDTO)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/pharmacy/medications/search")
                        .param("keyword", "Aspirin")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name", is("Aspirin")));
    }

    @Test
    void shouldGetMedicationsByType() throws Exception {
        mockMvc.perform(post("/api/pharmacy/medications")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicationDTO)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/pharmacy/medications/type/{type}", "TABLET")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type", is("TABLET")));
    }

    @Test
    void shouldGetMedicationsByManufacturer() throws Exception {
        mockMvc.perform(post("/api/pharmacy/medications")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicationDTO)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/pharmacy/medications/manufacturer/{manufacturer}", "PharmaCorp")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].manufacturer", is("PharmaCorp")));
    }
}
