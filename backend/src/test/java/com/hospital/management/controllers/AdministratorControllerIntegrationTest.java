package com.hospital.management.controllers;

import com.hospital.management.dto.AdministratorDTO;
import com.hospital.management.enums.UserRole;
import com.hospital.management.repositories.AdministratorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdministratorControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AdministratorRepository administratorRepository;

    private AdministratorDTO administratorDTO;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Clean up
        administratorRepository.deleteAll();
        
        // Create admin user and generate token
        testAuthUtils.createTestAdmin("admin@example.com");
        adminToken = testAuthUtils.generateToken("admin@example.com", UserRole.ADMIN);
        
        // Setup test DTO
        administratorDTO = new AdministratorDTO();
        administratorDTO.setFirstName("John");
        administratorDTO.setLastName("Admin");
        administratorDTO.setEmail("john.admin@hospital.com");
        administratorDTO.setPhone("1234567890");
        administratorDTO.setDepartment("IT");
        administratorDTO.setAccessLevel("FULL");
    }

    @Test
    void shouldCreateAdministrator() throws Exception {
        mockMvc.perform(post("/api/administrators")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(administratorDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Admin")))
                .andExpect(jsonPath("$.email", is("john.admin@hospital.com")))
                .andExpect(jsonPath("$.department", is("IT")))
                .andExpect(jsonPath("$.accessLevel", is("FULL")));
    }

    @Test
    void shouldReturn400WhenInvalidData() throws Exception {
        administratorDTO.setEmail("invalid-email");

        mockMvc.perform(post("/api/administrators")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(administratorDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAdministratorById() throws Exception {
        // Create administrator first
        String response = mockMvc.perform(post("/api/administrators")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(administratorDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AdministratorDTO created = objectMapper.readValue(response, AdministratorDTO.class);

        // Get by ID
        mockMvc.perform(get("/api/administrators/{id}", created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(created.getId().intValue())))
                .andExpect(jsonPath("$.email", is("john.admin@hospital.com")));
    }

    @Test
    void shouldReturn404WhenAdministratorNotFound() throws Exception {
        mockMvc.perform(get("/api/administrators/{id}", 999L)
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateAdministrator() throws Exception {
        // Create administrator first
        String response = mockMvc.perform(post("/api/administrators")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(administratorDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AdministratorDTO created = objectMapper.readValue(response, AdministratorDTO.class);

        // Update
        created.setDepartment("HR");
        created.setAccessLevel("LIMITED");

        mockMvc.perform(put("/api/administrators/{id}", created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department", is("HR")))
                .andExpect(jsonPath("$.accessLevel", is("LIMITED")));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentAdministrator() throws Exception {
        mockMvc.perform(put("/api/administrators/{id}", 999L)
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(administratorDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAdministrator() throws Exception {
        // Create administrator first
        String response = mockMvc.perform(post("/api/administrators")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(administratorDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AdministratorDTO created = objectMapper.readValue(response, AdministratorDTO.class);

        // Delete
        mockMvc.perform(delete("/api/administrators/{id}", created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/api/administrators/{id}", created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentAdministrator() throws Exception {
        mockMvc.perform(delete("/api/administrators/{id}", 999L)
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllAdministrators() throws Exception {
        // Create two administrators
        mockMvc.perform(post("/api/administrators")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(administratorDTO)))
                .andExpect(status().isCreated());

        AdministratorDTO admin2 = new AdministratorDTO();
        admin2.setFirstName("Jane");
        admin2.setLastName("Admin");
        admin2.setEmail("jane.admin@hospital.com");
        admin2.setPhone("0987654321");
        admin2.setDepartment("Finance");
        admin2.setAccessLevel("FULL");

        mockMvc.perform(post("/api/administrators")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(admin2)))
                .andExpect(status().isCreated());

        // Get all
        mockMvc.perform(get("/api/administrators")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3)); // Now includes the admin created in setUp
    }
}
