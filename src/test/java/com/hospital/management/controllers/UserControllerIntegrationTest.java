package com.hospital.management.controllers;

import com.hospital.management.config.TestSecurityConfig;
import com.hospital.management.entities.Doctor;
import com.hospital.management.entities.Patient;
import com.hospital.management.enums.Gender;
import com.hospital.management.enums.UserRole;
import com.hospital.management.repositories.DoctorRepository;
import com.hospital.management.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Patient");
        patient.setEmail("john.patient@test.com");
        patient.setPhone("1234567890");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender(Gender.MALE);
        patient.setBloodType("O+");
        patient.setAddress("123 Main St");
        patient.setEmergencyContact("Emergency Contact");
        patient.setInsuranceNumber("INS123");
        patient = patientRepository.save(patient);

        doctor = new Doctor();
        doctor.setFirstName("Jane");
        doctor.setLastName("Doctor");
        doctor.setEmail("jane.doctor@test.com");
        doctor.setPhone("0987654321");
        doctor.setSpecialization("Cardiology");
        doctor.setLicenseNumber("LIC456");
        doctor.setYearsOfExperience(10);
        doctor.setQualification("MD");
        doctor = doctorRepository.save(doctor);
    }

    @Test
    void shouldGetUserById() throws Exception {
        mockMvc.perform(get("/api/users/{id}", patient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(patient.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Patient")))
                .andExpect(jsonPath("$.email", is("john.patient@test.com")))
                .andExpect(jsonPath("$.role", is("PATIENT")));
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetUserByEmail() throws Exception {
        mockMvc.perform(get("/api/users/email/{email}", "john.patient@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("john.patient@test.com")))
                .andExpect(jsonPath("$.firstName", is("John")));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].email", hasItem("john.patient@test.com")))
                .andExpect(jsonPath("$[*].email", hasItem("jane.doctor@test.com")));
    }

    @Test
    void shouldGetUsersByRole() throws Exception {
        mockMvc.perform(get("/api/users/role/{role}", UserRole.PATIENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].role", is("PATIENT")));
    }

    @Test
    void shouldGetActiveUsers() throws Exception {
        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].active", everyItem(is(true))));
    }

    @Test
    void shouldGetUsersByRoleAndActive() throws Exception {
        mockMvc.perform(get("/api/users/role/{role}/active/{active}", UserRole.DOCTOR, true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].role", is("DOCTOR")))
                .andExpect(jsonPath("$[0].active", is(true)));
    }

    @Test
    void shouldCountByRole() throws Exception {
        mockMvc.perform(get("/api/users/count/role/{role}", UserRole.PATIENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", greaterThanOrEqualTo(1)));
    }

    @Test
    void shouldCountActiveUsers() throws Exception {
        mockMvc.perform(get("/api/users/count/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", greaterThanOrEqualTo(2)));
    }

    @Test
    void shouldSearchUsers() throws Exception {
        mockMvc.perform(get("/api/users/search").param("keyword", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].firstName", containsStringIgnoringCase("john")));
    }
}
