package com.hospital.management.controllers;

import com.hospital.management.dto.DoctorDTO;
import com.hospital.management.entities.Administrator;
import com.hospital.management.entities.Hospital;
import com.hospital.management.enums.UserRole;
import com.hospital.management.repositories.AdministratorRepository;
import com.hospital.management.repositories.DoctorRepository;
import com.hospital.management.repositories.HospitalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DoctorControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    private Hospital testHospital;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Clean up
        doctorRepository.deleteAll();
        administratorRepository.deleteAll();
        hospitalRepository.deleteAll();
        
        // Create test hospital
        testHospital = testAuthUtils.createTestHospital("Test Hospital");
        
        // Create admin user and generate token
        Administrator admin = testAuthUtils.createTestAdmin("admin@example.com");
        adminToken = testAuthUtils.generateToken("admin@example.com", UserRole.ADMIN);
    }

    @Test
    void shouldCreateDoctor() throws Exception {
        DoctorDTO dto = createDoctorDTO("Dr. John", "Smith", "john.smith@hospital.com", 
                "+1234567890", "Cardiology", "LIC12345", 10, "MD, FACC");

        mockMvc.perform(post("/api/doctors")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
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
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void shouldGetDoctorById() throws Exception {
        DoctorDTO created = createAndSaveDoctor("Dr. Jane", "Doe", "jane.doe@hospital.com",
                "+9876543210", "Neurology", "LIC67890", 15, "MD, PhD");

        mockMvc.perform(get("/api/doctors/" + created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.firstName").value("Dr. Jane"))
                .andExpect(jsonPath("$.specialization").value("Neurology"));
    }

    @Test
    void shouldReturn404WhenDoctorNotFound() throws Exception {
        mockMvc.perform(get("/api/doctors/999")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
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
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
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
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteDoctor() throws Exception {
        DoctorDTO created = createAndSaveDoctor("Dr. Bob", "Wilson", "bob.wilson@hospital.com",
                "+2222222222", "Orthopedics", "LIC22222", 20, "MD, FAAOS");

        mockMvc.perform(delete("/api/doctors/" + created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/doctors/" + created.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentDoctor() throws Exception {
        mockMvc.perform(delete("/api/doctors/999")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllDoctors() throws Exception {
        createAndSaveDoctor("Dr. Doctor1", "One", "doctor1@hospital.com",
                "+3333333333", "Cardiology", "LIC33333", 8, "MD");
        createAndSaveDoctor("Dr. Doctor2", "Two", "doctor2@hospital.com",
                "+4444444444", "Neurology", "LIC44444", 12, "MD, PhD");

        mockMvc.perform(get("/api/doctors")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
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
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .param("specialization", "Cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].specialization").value("Cardiology"))
                .andExpect(jsonPath("$[1].specialization").value("Cardiology"));
    }

    // Phase 10.11: Owner-based authorization tests for doctors
    @Test
    void doctorShouldAccessTheirOwnProfile() throws Exception {
        // Create a doctor
        DoctorDTO doctor = createAndSaveDoctor("Dr. John", "Smith", "john.smith@hospital.com",
                "+1234567890", "Cardiology", "LIC12345", 10, "MD");

        // Generate token for this doctor
        String doctorToken = testAuthUtils.generateToken("john.smith@hospital.com", UserRole.DOCTOR);

        // Doctor should be able to access their own profile
        mockMvc.perform(get("/api/doctors/" + doctor.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(doctorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(doctor.getId()))
                .andExpect(jsonPath("$.email").value("john.smith@hospital.com"));
    }

    @Test
    void doctorShouldNotAccessOtherDoctorsProfile() throws Exception {
        // Create two doctors
        DoctorDTO doctor1 = createAndSaveDoctor("Dr. John", "Smith", "john.smith@hospital.com",
                "+1234567890", "Cardiology", "LIC12345", 10, "MD");
        DoctorDTO doctor2 = createAndSaveDoctor("Dr. Jane", "Doe", "jane.doe@hospital.com",
                "+9876543210", "Neurology", "LIC67890", 15, "MD");

        // Generate token for doctor1
        String doctor1Token = testAuthUtils.generateToken("john.smith@hospital.com", UserRole.DOCTOR);

        // Doctor1 should NOT be able to access doctor2's profile
        mockMvc.perform(get("/api/doctors/" + doctor2.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(doctor1Token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied: You do not have permission to access this resource"));
    }

    @Test
    void doctorShouldUpdateTheirOwnProfile() throws Exception {
        // Create a doctor
        DoctorDTO doctor = createAndSaveDoctor("Dr. John", "Smith", "john.smith@hospital.com",
                "+1234567890", "Cardiology", "LIC12345", 10, "MD");

        // Generate token for this doctor
        String doctorToken = testAuthUtils.generateToken("john.smith@hospital.com", UserRole.DOCTOR);

        // Update doctor's own profile
        doctor.setYearsOfExperience(12);
        doctor.setQualification("MD, FACC");

        mockMvc.perform(put("/api/doctors/" + doctor.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(doctorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doctor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearsOfExperience").value(12))
                .andExpect(jsonPath("$.qualification").value("MD, FACC"));
    }

    @Test
    void doctorShouldNotUpdateOtherDoctorsProfile() throws Exception {
        // Create two doctors
        DoctorDTO doctor1 = createAndSaveDoctor("Dr. John", "Smith", "john.smith@hospital.com",
                "+1234567890", "Cardiology", "LIC12345", 10, "MD");
        DoctorDTO doctor2 = createAndSaveDoctor("Dr. Jane", "Doe", "jane.doe@hospital.com",
                "+9876543210", "Neurology", "LIC67890", 15, "MD");

        // Generate token for doctor1
        String doctor1Token = testAuthUtils.generateToken("john.smith@hospital.com", UserRole.DOCTOR);

        // Doctor1 should NOT be able to update doctor2's profile
        doctor2.setYearsOfExperience(20);

        mockMvc.perform(put("/api/doctors/" + doctor2.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(doctor1Token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doctor2)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied: You do not have permission to access this resource"));
    }

    @Test
    void patientShouldNotAccessDoctorProfile() throws Exception {
        // Create a doctor
        DoctorDTO doctor = createAndSaveDoctor("Dr. John", "Smith", "john.smith@hospital.com",
                "+1234567890", "Cardiology", "LIC12345", 10, "MD");

        // Create a patient and generate token
        testAuthUtils.createTestPatient("patient@email.com", testHospital);
        String patientToken = testAuthUtils.generateToken("patient@email.com", UserRole.PATIENT);

        // Patient should NOT be able to access doctor's profile
        mockMvc.perform(get("/api/doctors/" + doctor.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(patientToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied: You do not have permission to access this resource"));
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
        dto.setHospitalId(testHospital.getId());
        return dto;
    }

    private DoctorDTO createAndSaveDoctor(String firstName, String lastName, String email,
                                          String phone, String specialization, String licenseNumber,
                                          Integer yearsOfExperience, String qualification) throws Exception {
        DoctorDTO dto = createDoctorDTO(firstName, lastName, email, phone, specialization,
                licenseNumber, yearsOfExperience, qualification);

        String response = mockMvc.perform(post("/api/doctors")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, DoctorDTO.class);
    }
}
