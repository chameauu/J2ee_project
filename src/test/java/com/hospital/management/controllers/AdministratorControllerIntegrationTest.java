package com.hospital.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.management.config.TestSecurityConfig;
import com.hospital.management.dto.AdministratorDTO;
import com.hospital.management.repositories.AdministratorRepository;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class AdministratorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdministratorRepository administratorRepository;

    private AdministratorDTO administratorDTO;

    @BeforeEach
    void setUp() {
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(administratorDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAdministratorById() throws Exception {
        // Create administrator first
        String response = mockMvc.perform(post("/api/administrators")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(administratorDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AdministratorDTO created = objectMapper.readValue(response, AdministratorDTO.class);

        // Get by ID
        mockMvc.perform(get("/api/administrators/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(created.getId().intValue())))
                .andExpect(jsonPath("$.email", is("john.admin@hospital.com")));
    }

    @Test
    void shouldReturn404WhenAdministratorNotFound() throws Exception {
        mockMvc.perform(get("/api/administrators/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateAdministrator() throws Exception {
        // Create administrator first
        String response = mockMvc.perform(post("/api/administrators")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(administratorDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AdministratorDTO created = objectMapper.readValue(response, AdministratorDTO.class);

        // Update
        created.setDepartment("HR");
        created.setAccessLevel("LIMITED");

        mockMvc.perform(put("/api/administrators/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department", is("HR")))
                .andExpect(jsonPath("$.accessLevel", is("LIMITED")));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentAdministrator() throws Exception {
        mockMvc.perform(put("/api/administrators/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(administratorDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAdministrator() throws Exception {
        // Create administrator first
        String response = mockMvc.perform(post("/api/administrators")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(administratorDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AdministratorDTO created = objectMapper.readValue(response, AdministratorDTO.class);

        // Delete
        mockMvc.perform(delete("/api/administrators/{id}", created.getId()))
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/api/administrators/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentAdministrator() throws Exception {
        mockMvc.perform(delete("/api/administrators/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllAdministrators() throws Exception {
        // Create two administrators
        mockMvc.perform(post("/api/administrators")
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(admin2)))
                .andExpect(status().isCreated());

        // Get all
        mockMvc.perform(get("/api/administrators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
