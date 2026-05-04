package com.hospital.management.controllers;

import com.hospital.management.entities.*;
import com.hospital.management.enums.AppointmentStatus;
import com.hospital.management.enums.AppointmentType;
import com.hospital.management.enums.Gender;
import com.hospital.management.enums.PrescriptionStatus;
import com.hospital.management.enums.UserRole;
import com.hospital.management.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StatisticsControllerIntegrationTest extends BaseIntegrationTest {

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

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    private Hospital hospital;
    private Doctor doctor;
    private Patient patient;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Clean up
        appointmentRepository.deleteAll();
        prescriptionRepository.deleteAll();
        medicalRecordRepository.deleteAll();
        pharmacistRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        administratorRepository.deleteAll();
        hospitalRepository.deleteAll();
        
        // Create hospital
        hospital = new Hospital();
        hospital.setName("Test Hospital");
        hospital.setAddress("123 Test St");
        hospital.setPhone("555-0001");
        hospital.setEmail("test@hospital.com");
        hospital.setRegistrationNumber("REG-TEST-001");
        hospital.setEstablishedDate(LocalDate.of(2000, 1, 1));
        hospital = hospitalRepository.save(hospital);

        // Create admin user and generate token
        testAuthUtils.createTestAdmin("admin@example.com");
        adminToken = testAuthUtils.generateToken("admin@example.com", UserRole.ADMIN);

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
        doctor.setHospital(hospital);
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
        patient.setHospital(hospital);
        patient = patientRepository.save(patient);

        Pharmacist pharmacist = new Pharmacist();
        pharmacist.setFirstName("Bob");
        pharmacist.setLastName("Johnson");
        pharmacist.setEmail("bob.johnson@hospital.com");
        pharmacist.setPhone("1112223333");
        pharmacist.setLicenseNumber("PLIC001");
        pharmacist.setQualification("PharmD");
        pharmacist.setHospital(hospital);
        pharmacistRepository.save(pharmacist);

        // Create appointment for today
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setHospital(hospital);
        appointment.setAppointmentDateTime(LocalDateTime.now().plusHours(2));
        appointment.setDurationMinutes(30);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setType(AppointmentType.CONSULTATION);
        appointment.setReason("Checkup");
        appointmentRepository.save(appointment);

        // Create completed appointment
        Appointment completedAppointment = new Appointment();
        completedAppointment.setPatient(patient);
        completedAppointment.setDoctor(doctor);
        completedAppointment.setHospital(hospital);
        completedAppointment.setAppointmentDateTime(LocalDateTime.now().minusDays(1));
        completedAppointment.setDurationMinutes(30);
        completedAppointment.setStatus(AppointmentStatus.COMPLETED);
        completedAppointment.setType(AppointmentType.CONSULTATION);
        completedAppointment.setReason("Follow-up");
        appointmentRepository.save(completedAppointment);

        // Create medical record
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setPatient(patient);
        medicalRecord.setDoctor(doctor);
        medicalRecord.setHospital(hospital);
        medicalRecord.setVisitDate(LocalDateTime.now());
        medicalRecord.setChiefComplaint("Chest pain");
        medicalRecord.setDiagnosis("Angina");
        medicalRecord.setTreatment("Medication");
        medicalRecord = medicalRecordRepository.save(medicalRecord);

        // Create active prescription
        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setMedicalRecord(medicalRecord);
        prescription.setHospital(hospital);
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
    void shouldGetDashboardStats() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDoctors", is(1)))
                .andExpect(jsonPath("$.totalPatients", is(1)))
                .andExpect(jsonPath("$.totalPharmacists", is(1)))
                .andExpect(jsonPath("$.todaysAppointments", is(1)))
                .andExpect(jsonPath("$.completedAppointments", is(1)))
                .andExpect(jsonPath("$.activePrescriptions", is(1)))
                .andExpect(jsonPath("$.totalMedicalRecords", is(1)));
    }

    @Test
    void shouldGetDoctorStats() throws Exception {
        mockMvc.perform(get("/api/doctors/{doctorId}/statistics", doctor.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId", is(doctor.getId().intValue())))
                .andExpect(jsonPath("$.doctorName", is("John Doe")))
                .andExpect(jsonPath("$.totalPatients", is(1)))
                .andExpect(jsonPath("$.totalAppointments", is(2)))
                .andExpect(jsonPath("$.todaysAppointments", is(1)))
                .andExpect(jsonPath("$.completedAppointments", is(1)))
                .andExpect(jsonPath("$.totalMedicalRecords", is(1)))
                .andExpect(jsonPath("$.totalPrescriptions", is(1)));
    }

    @Test
    void shouldReturn404WhenDoctorNotFound() throws Exception {
        mockMvc.perform(get("/api/doctors/{doctorId}/statistics", 999L)
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(adminToken)))
                .andExpect(status().isNotFound());
    }
}
