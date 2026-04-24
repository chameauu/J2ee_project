package com.hospital.management.controllers;

import com.hospital.management.config.TestSecurityConfig;
import com.hospital.management.entities.*;
import com.hospital.management.enums.AppointmentStatus;
import com.hospital.management.enums.AppointmentType;
import com.hospital.management.enums.Gender;
import com.hospital.management.enums.PrescriptionStatus;
import com.hospital.management.repositories.*;
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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class DirectorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PharmacistRepository pharmacistRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        // Create test data
        doctor = new Doctor();
        doctor.setFirstName("John");
        doctor.setLastName("Doe");
        doctor.setEmail("john.doe@hospital.com");
        doctor.setPhone("1234567890");
        doctor.setSpecialization("Cardiology");
        doctor.setLicenseNumber("LIC001");
        doctor.setYearsOfExperience(10);
        doctor.setQualification("MD");
        doctor = doctorRepository.save(doctor);

        patient = new Patient();
        patient.setFirstName("Jane");
        patient.setLastName("Smith");
        patient.setEmail("jane.smith@example.com");
        patient.setPhone("0987654321");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender(Gender.FEMALE);
        patient.setBloodType("O+");
        patient.setAddress("123 Main St");
        patient = patientRepository.save(patient);

        Pharmacist pharmacist = new Pharmacist();
        pharmacist.setFirstName("Bob");
        pharmacist.setLastName("Johnson");
        pharmacist.setEmail("bob.johnson@hospital.com");
        pharmacist.setPhone("1112223333");
        pharmacist.setLicenseNumber("PLIC001");
        pharmacist.setQualification("PharmD");
        pharmacistRepository.save(pharmacist);

        // Create appointments
        Appointment appointment1 = new Appointment();
        appointment1.setPatient(patient);
        appointment1.setDoctor(doctor);
        appointment1.setAppointmentDateTime(LocalDateTime.now().plusHours(2));
        appointment1.setDurationMinutes(30);
        appointment1.setStatus(AppointmentStatus.SCHEDULED);
        appointment1.setType(AppointmentType.CONSULTATION);
        appointment1.setReason("Checkup");
        appointmentRepository.save(appointment1);

        Appointment appointment2 = new Appointment();
        appointment2.setPatient(patient);
        appointment2.setDoctor(doctor);
        appointment2.setAppointmentDateTime(LocalDateTime.now().minusDays(1));
        appointment2.setDurationMinutes(30);
        appointment2.setStatus(AppointmentStatus.COMPLETED);
        appointment2.setType(AppointmentType.CONSULTATION);
        appointment2.setReason("Follow-up");
        appointmentRepository.save(appointment2);

        // Create medical record
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setPatient(patient);
        medicalRecord.setDoctor(doctor);
        medicalRecord.setVisitDate(LocalDateTime.now());
        medicalRecord.setChiefComplaint("Chest pain");
        medicalRecord.setDiagnosis("Angina");
        medicalRecord.setTreatment("Medication");
        medicalRecord = medicalRecordRepository.save(medicalRecord);

        // Create prescription
        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setMedicalRecord(medicalRecord);
        prescription.setPrescribedDate(LocalDateTime.now());
        prescription.setValidUntil(LocalDate.now().plusDays(30));
        prescription.setStatus(PrescriptionStatus.ACTIVE);
        prescription.setMedicationName("Aspirin");
        prescription.setDosage("100mg");
        prescription.setFrequency("Once daily");
        prescription.setDurationDays(30);
        prescriptionRepository.save(prescription);
    }

    @Test
    void shouldGetDirectorDashboard() throws Exception {
        mockMvc.perform(get("/api/director/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDoctors", is(1)))
                .andExpect(jsonPath("$.totalPatients", is(1)))
                .andExpect(jsonPath("$.totalPharmacists", is(1)))
                .andExpect(jsonPath("$.totalAppointments", is(2)))
                .andExpect(jsonPath("$.completedAppointments", is(1)))
                .andExpect(jsonPath("$.scheduledAppointments", is(1)))
                .andExpect(jsonPath("$.appointmentCompletionRate", is(50.0)))
                .andExpect(jsonPath("$.averageAppointmentsPerDoctor", is(2.0)))
                .andExpect(jsonPath("$.activePrescriptions", is(1)));
    }

    @Test
    void shouldGetAllDoctorsPerformance() throws Exception {
        mockMvc.perform(get("/api/director/doctors/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].doctorId", is(doctor.getId().intValue())))
                .andExpect(jsonPath("$[0].doctorName", is("John Doe")))
                .andExpect(jsonPath("$[0].specialization", is("Cardiology")))
                .andExpect(jsonPath("$[0].totalAppointments", is(2)))
                .andExpect(jsonPath("$[0].completedAppointments", is(1)))
                .andExpect(jsonPath("$[0].completionRate", is(50.0)));
    }

    @Test
    void shouldGetDoctorPerformance() throws Exception {
        mockMvc.perform(get("/api/director/doctors/{doctorId}/performance", doctor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId", is(doctor.getId().intValue())))
                .andExpect(jsonPath("$.doctorName", is("John Doe")))
                .andExpect(jsonPath("$.specialization", is("Cardiology")))
                .andExpect(jsonPath("$.totalPatients", is(1)))
                .andExpect(jsonPath("$.totalAppointments", is(2)))
                .andExpect(jsonPath("$.completedAppointments", is(1)))
                .andExpect(jsonPath("$.completionRate", is(50.0)))
                .andExpect(jsonPath("$.totalMedicalRecords", is(1)))
                .andExpect(jsonPath("$.totalPrescriptions", is(1)));
    }

    @Test
    void shouldReturn404WhenDoctorNotFoundForPerformance() throws Exception {
        mockMvc.perform(get("/api/director/doctors/{doctorId}/performance", 999L))
                .andExpect(status().isNotFound());
    }
}
