package com.hospital.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.management.config.TestSecurityConfig;
import com.hospital.management.dto.HospitalDTO;
import com.hospital.management.dto.MedicalRecordDTO;
import com.hospital.management.entities.Doctor;
import com.hospital.management.entities.Hospital;
import com.hospital.management.entities.Patient;
import com.hospital.management.enums.Gender;
import com.hospital.management.repositories.DoctorRepository;
import com.hospital.management.repositories.HospitalRepository;
import com.hospital.management.repositories.MedicalRecordRepository;
import com.hospital.management.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class MedicalRecordControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private HospitalRepository hospitalRepository; // Phase 10.6

    private Hospital testHospital; // Phase 10.6
    private Patient testPatient;
    private Doctor testDoctor;

    @BeforeEach
    void setUp() {
        medicalRecordRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
        hospitalRepository.deleteAll();

        // Phase 10.6: Create test hospital first
        testHospital = new Hospital();
        testHospital.setName("Test Hospital");
        testHospital.setAddress("123 Test St");
        testHospital.setPhone("555-0001");
        testHospital.setEmail("test@hospital.com");
        testHospital.setRegistrationNumber("REG-TEST-001");
        testHospital.setEstablishedDate(LocalDate.of(2000, 1, 1));
        testHospital = hospitalRepository.save(testHospital);

        // Create test patient
        testPatient = new Patient();
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setEmail("john.doe@test.com");
        testPatient.setPhone("1234567890");
        testPatient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testPatient.setGender(Gender.MALE);
        testPatient.setHospital(testHospital); // Phase 10.6
        testPatient = patientRepository.save(testPatient);

        // Create test doctor
        testDoctor = new Doctor();
        testDoctor.setFirstName("Dr. Jane");
        testDoctor.setLastName("Smith");
        testDoctor.setEmail("jane.smith@test.com");
        testDoctor.setPhone("0987654321");
        testDoctor.setSpecialization("Cardiology");
        testDoctor.setLicenseNumber("DOC123");
        testDoctor.setYearsOfExperience(10);
        testDoctor.setHospital(testHospital); // Phase 10.6
        testDoctor = doctorRepository.save(testDoctor);
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void shouldCreateMedicalRecord() throws Exception {
        // Given
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setPatientId(testPatient.getId());
        dto.setDoctorId(testDoctor.getId());
        dto.setChiefComplaint("Chest pain");
        dto.setDiagnosis("Angina");
        dto.setTreatment("Medication prescribed");
        dto.setNotes("Patient advised to rest");

        // When & Then
        mockMvc.perform(post("/api/medical-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.patientId").value(testPatient.getId()))
                .andExpect(jsonPath("$.doctorId").value(testDoctor.getId()))
                .andExpect(jsonPath("$.chiefComplaint").value("Chest pain"))
                .andExpect(jsonPath("$.diagnosis").value("Angina"))
                .andExpect(jsonPath("$.treatment").value("Medication prescribed"))
                .andExpect(jsonPath("$.notes").value("Patient advised to rest"))
                .andExpect(jsonPath("$.visitDate").exists());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void shouldReturn400WhenInvalidData() throws Exception {
        // Given - missing required fields
        MedicalRecordDTO dto = new MedicalRecordDTO();

        // When & Then
        mockMvc.perform(post("/api/medical-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void shouldGetMedicalRecordById() throws Exception {
        // Given - create a medical record first
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setPatientId(testPatient.getId());
        dto.setDoctorId(testDoctor.getId());
        dto.setChiefComplaint("Headache");
        dto.setDiagnosis("Migraine");
        dto.setTreatment("Pain relievers");

        String response = mockMvc.perform(post("/api/medical-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();

        Long recordId = objectMapper.readTree(response).get("id").asLong();

        // When & Then
        mockMvc.perform(get("/api/medical-records/{id}", recordId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(recordId))
                .andExpect(jsonPath("$.chiefComplaint").value("Headache"))
                .andExpect(jsonPath("$.diagnosis").value("Migraine"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void shouldReturn404WhenMedicalRecordNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/medical-records/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void shouldUpdateMedicalRecord() throws Exception {
        // Given - create a medical record first
        MedicalRecordDTO createDto = new MedicalRecordDTO();
        createDto.setPatientId(testPatient.getId());
        createDto.setDoctorId(testDoctor.getId());
        createDto.setChiefComplaint("Fever");
        createDto.setDiagnosis("Viral infection");
        createDto.setTreatment("Rest and fluids");

        String response = mockMvc.perform(post("/api/medical-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andReturn().getResponse().getContentAsString();

        Long recordId = objectMapper.readTree(response).get("id").asLong();

        // Update DTO
        MedicalRecordDTO updateDto = new MedicalRecordDTO();
        updateDto.setPatientId(testPatient.getId());
        updateDto.setDoctorId(testDoctor.getId());
        updateDto.setChiefComplaint("Fever");
        updateDto.setDiagnosis("Bacterial infection");
        updateDto.setTreatment("Antibiotics prescribed");
        updateDto.setNotes("Follow-up in 1 week");

        // When & Then
        mockMvc.perform(put("/api/medical-records/{id}", recordId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(recordId))
                .andExpect(jsonPath("$.diagnosis").value("Bacterial infection"))
                .andExpect(jsonPath("$.treatment").value("Antibiotics prescribed"))
                .andExpect(jsonPath("$.notes").value("Follow-up in 1 week"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void shouldReturn404WhenUpdatingNonExistentMedicalRecord() throws Exception {
        // Given
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setPatientId(testPatient.getId());
        dto.setDoctorId(testDoctor.getId());
        dto.setChiefComplaint("Test");
        dto.setDiagnosis("Test");
        dto.setTreatment("Test");

        // When & Then
        mockMvc.perform(put("/api/medical-records/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void shouldGetPatientMedicalHistory() throws Exception {
        // Given - create multiple medical records for the patient
        for (int i = 0; i < 3; i++) {
            MedicalRecordDTO dto = new MedicalRecordDTO();
            dto.setPatientId(testPatient.getId());
            dto.setDoctorId(testDoctor.getId());
            dto.setChiefComplaint("Complaint " + i);
            dto.setDiagnosis("Diagnosis " + i);
            dto.setTreatment("Treatment " + i);

            mockMvc.perform(post("/api/medical-records")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));
        }

        // When & Then
        mockMvc.perform(get("/api/patients/{patientId}/medical-records", testPatient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].patientId").value(testPatient.getId()));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void shouldGetDoctorMedicalRecords() throws Exception {
        // Given - create multiple medical records by the doctor
        for (int i = 0; i < 2; i++) {
            MedicalRecordDTO dto = new MedicalRecordDTO();
            dto.setPatientId(testPatient.getId());
            dto.setDoctorId(testDoctor.getId());
            dto.setChiefComplaint("Complaint " + i);
            dto.setDiagnosis("Diagnosis " + i);
            dto.setTreatment("Treatment " + i);

            mockMvc.perform(post("/api/medical-records")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));
        }

        // When & Then
        mockMvc.perform(get("/api/doctors/{doctorId}/medical-records", testDoctor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].doctorId").value(testDoctor.getId()));
    }
}
