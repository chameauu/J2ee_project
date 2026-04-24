package com.hospital.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.management.config.TestSecurityConfig;
import com.hospital.management.dto.HospitalDirectorDTO;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class HospitalDirectorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private HospitalDirectorDTO hospitalDirectorDTO;

    @BeforeEach
    void setUp() {
        hospitalDirectorDTO = new HospitalDirectorDTO();
        hospitalDirectorDTO.setFirstName("John");
        hospitalDirectorDTO.setLastName("Director");
        hospitalDirectorDTO.setEmail("john.director@hospital.com");
        hospitalDirectorDTO.setPhone("1234567890");
        hospitalDirectorDTO.setHospitalName("City Hospital");
        hospitalDirectorDTO.setAppointmentDate(LocalDate.of(2020, 1, 1));
        hospitalDirectorDTO.setCredentials("MD, MBA, FACHE");
    }

    @Test
    void shouldCreateHospitalDirector() throws Exception {
        mockMvc.perform(post("/api/hospital-directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hospitalDirectorDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Director")))
                .andExpect(jsonPath("$.email", is("john.director@hospital.com")))
                .andExpect(jsonPath("$.hospitalName", is("City Hospital")))
                .andExpect(jsonPath("$.credentials", is("MD, MBA, FACHE")));
    }

    @Test
    void shouldReturn400WhenInvalidData() throws Exception {
        hospitalDirectorDTO.setEmail("invalid-email");

        mockMvc.perform(post("/api/hospital-directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hospitalDirectorDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetHospitalDirectorById() throws Exception {
        // Create hospital director first
        String response = mockMvc.perform(post("/api/hospital-directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hospitalDirectorDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        HospitalDirectorDTO created = objectMapper.readValue(response, HospitalDirectorDTO.class);

        // Get by ID
        mockMvc.perform(get("/api/hospital-directors/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(created.getId().intValue())))
                .andExpect(jsonPath("$.email", is("john.director@hospital.com")));
    }

    @Test
    void shouldReturn404WhenHospitalDirectorNotFound() throws Exception {
        mockMvc.perform(get("/api/hospital-directors/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateHospitalDirector() throws Exception {
        // Create hospital director first
        String response = mockMvc.perform(post("/api/hospital-directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hospitalDirectorDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        HospitalDirectorDTO created = objectMapper.readValue(response, HospitalDirectorDTO.class);

        // Update
        created.setHospitalName("Updated Hospital");
        created.setCredentials("MD, PhD, FACHE");

        mockMvc.perform(put("/api/hospital-directors/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hospitalName", is("Updated Hospital")))
                .andExpect(jsonPath("$.credentials", is("MD, PhD, FACHE")));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentHospitalDirector() throws Exception {
        mockMvc.perform(put("/api/hospital-directors/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hospitalDirectorDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteHospitalDirector() throws Exception {
        // Create hospital director first
        String response = mockMvc.perform(post("/api/hospital-directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hospitalDirectorDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        HospitalDirectorDTO created = objectMapper.readValue(response, HospitalDirectorDTO.class);

        // Delete
        mockMvc.perform(delete("/api/hospital-directors/{id}", created.getId()))
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/api/hospital-directors/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentHospitalDirector() throws Exception {
        mockMvc.perform(delete("/api/hospital-directors/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllHospitalDirectors() throws Exception {
        // Create two hospital directors
        mockMvc.perform(post("/api/hospital-directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hospitalDirectorDTO)))
                .andExpect(status().isCreated());

        HospitalDirectorDTO director2 = new HospitalDirectorDTO();
        director2.setFirstName("Jane");
        director2.setLastName("Director");
        director2.setEmail("jane.director@hospital.com");
        director2.setPhone("0987654321");
        director2.setHospitalName("Regional Hospital");
        director2.setAppointmentDate(LocalDate.of(2021, 6, 1));
        director2.setCredentials("MD, MBA");

        mockMvc.perform(post("/api/hospital-directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(director2)))
                .andExpect(status().isCreated());

        // Get all
        mockMvc.perform(get("/api/hospital-directors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
