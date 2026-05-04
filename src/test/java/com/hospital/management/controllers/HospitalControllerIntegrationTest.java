package com.hospital.management.controllers;

import com.hospital.management.dto.HospitalDTO;
import com.hospital.management.entities.Administrator;
import com.hospital.management.entities.Hospital;
import com.hospital.management.enums.UserRole;
import com.hospital.management.repositories.AdministratorRepository;
import com.hospital.management.repositories.HospitalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HospitalControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    private String adminToken;

    @BeforeEach
    void setUp() {
        // Clean up
        hospitalRepository.deleteAll();
        administratorRepository.deleteAll();
        
        // Create admin user and generate token
        Administrator admin = testAuthUtils.createTestAdmin("admin@example.com");
        adminToken = testAuthUtils.generateToken("admin@example.com", UserRole.ADMIN);
    }

    @Test
    void shouldCreateHospital() throws Exception {
        HospitalDTO hospitalDTO = new HospitalDTO();
        hospitalDTO.setName("City General Hospital");
        hospitalDTO.setAddress("123 Main Street, City, State 12345");
        hospitalDTO.setPhone("+1-555-0100");
        hospitalDTO.setEmail("info@cityhospital.com");
        hospitalDTO.setRegistrationNumber("REG-2024-001");
        hospitalDTO.setEstablishedDate(LocalDate.of(1990, 1, 15));

        mockMvc.perform(post("/api/hospitals")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hospitalDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("City General Hospital"))
                .andExpect(jsonPath("$.address").value("123 Main Street, City, State 12345"))
                .andExpect(jsonPath("$.phone").value("+1-555-0100"))
                .andExpect(jsonPath("$.email").value("info@cityhospital.com"))
                .andExpect(jsonPath("$.registrationNumber").value("REG-2024-001"))
                .andExpect(jsonPath("$.establishedDate").value("1990-01-15"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldReturn400WhenInvalidData() throws Exception {
        HospitalDTO hospitalDTO = new HospitalDTO();
        // Missing required fields

        mockMvc.perform(post("/api/hospitals")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hospitalDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetHospitalById() throws Exception {
        Hospital hospital = new Hospital();
        hospital.setName("County Medical Center");
        hospital.setAddress("456 Oak Avenue, County, State 67890");
        hospital.setPhone("+1-555-0200");
        hospital.setEmail("contact@countymedical.com");
        hospital.setRegistrationNumber("REG-2024-002");
        hospital.setEstablishedDate(LocalDate.of(1985, 6, 20));
        hospital = hospitalRepository.save(hospital);

        mockMvc.perform(get("/api/hospitals/" + hospital.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(hospital.getId()))
                .andExpect(jsonPath("$.name").value("County Medical Center"))
                .andExpect(jsonPath("$.registrationNumber").value("REG-2024-002"));
    }

    @Test
    void shouldReturn404WhenHospitalNotFound() throws Exception {
        mockMvc.perform(get("/api/hospitals/99999")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Hospital not found with id: 99999"));
    }

    @Test
    void shouldUpdateHospital() throws Exception {
        Hospital hospital = new Hospital();
        hospital.setName("Regional Health Clinic");
        hospital.setAddress("789 Pine Road, Region, State 11111");
        hospital.setPhone("+1-555-0300");
        hospital.setEmail("info@regionalhealth.com");
        hospital.setRegistrationNumber("REG-2024-003");
        hospital.setEstablishedDate(LocalDate.of(2000, 3, 10));
        hospital = hospitalRepository.save(hospital);

        HospitalDTO updateDTO = new HospitalDTO();
        updateDTO.setName("Regional Health Clinic - Updated");
        updateDTO.setAddress("789 Pine Road, Region, State 11111");
        updateDTO.setPhone("+1-555-0301");
        updateDTO.setEmail("contact@regionalhealth.com");
        updateDTO.setRegistrationNumber("REG-2024-003");
        updateDTO.setEstablishedDate(LocalDate.of(2000, 3, 10));

        mockMvc.perform(put("/api/hospitals/" + hospital.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Regional Health Clinic - Updated"))
                .andExpect(jsonPath("$.phone").value("+1-555-0301"))
                .andExpect(jsonPath("$.email").value("contact@regionalhealth.com"));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentHospital() throws Exception {
        HospitalDTO updateDTO = new HospitalDTO();
        updateDTO.setName("Non-existent Hospital");
        updateDTO.setAddress("Address");
        updateDTO.setPhone("+1-555-0000");
        updateDTO.setEmail("test@test.com");
        updateDTO.setRegistrationNumber("REG-9999");
        updateDTO.setEstablishedDate(LocalDate.now());

        mockMvc.perform(put("/api/hospitals/99999")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteHospital() throws Exception {
        Hospital hospital = new Hospital();
        hospital.setName("Test Hospital");
        hospital.setAddress("Test Address");
        hospital.setPhone("+1-555-0400");
        hospital.setEmail("test@hospital.com");
        hospital.setRegistrationNumber("REG-2024-004");
        hospital.setEstablishedDate(LocalDate.now());
        hospital = hospitalRepository.save(hospital);

        mockMvc.perform(delete("/api/hospitals/" + hospital.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/hospitals/" + hospital.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentHospital() throws Exception {
        mockMvc.perform(delete("/api/hospitals/99999")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllHospitals() throws Exception {
        Hospital hospital1 = new Hospital();
        hospital1.setName("Hospital One");
        hospital1.setAddress("Address 1");
        hospital1.setPhone("+1-555-1000");
        hospital1.setEmail("one@hospital.com");
        hospital1.setRegistrationNumber("REG-2024-101");
        hospital1.setEstablishedDate(LocalDate.now());
        hospitalRepository.save(hospital1);

        Hospital hospital2 = new Hospital();
        hospital2.setName("Hospital Two");
        hospital2.setAddress("Address 2");
        hospital2.setPhone("+1-555-2000");
        hospital2.setEmail("two@hospital.com");
        hospital2.setRegistrationNumber("REG-2024-102");
        hospital2.setEstablishedDate(LocalDate.now());
        hospitalRepository.save(hospital2);

        mockMvc.perform(get("/api/hospitals")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Hospital One"))
                .andExpect(jsonPath("$[1].name").value("Hospital Two"));
    }

    @Test
    void shouldSearchHospitalsByName() throws Exception {
        Hospital hospital1 = new Hospital();
        hospital1.setName("City General Hospital");
        hospital1.setAddress("Address 1");
        hospital1.setPhone("+1-555-1001");
        hospital1.setEmail("city@hospital.com");
        hospital1.setRegistrationNumber("REG-2024-201");
        hospital1.setEstablishedDate(LocalDate.now());
        hospitalRepository.save(hospital1);

        Hospital hospital2 = new Hospital();
        hospital2.setName("County Medical Center");
        hospital2.setAddress("Address 2");
        hospital2.setPhone("+1-555-2001");
        hospital2.setEmail("county@hospital.com");
        hospital2.setRegistrationNumber("REG-2024-202");
        hospital2.setEstablishedDate(LocalDate.now());
        hospitalRepository.save(hospital2);

        mockMvc.perform(get("/api/hospitals/search")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .param("keyword", "City"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("City General Hospital"));
    }
}
