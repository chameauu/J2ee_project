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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DirectorDashboardControllerIntegrationTest extends BaseIntegrationTest {

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
    private HospitalDirectorRepository hospitalDirectorRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    private Hospital hospital;
    private Doctor doctor;
    private Patient patient;
    private String directorToken;
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
        hospitalDirectorRepository.deleteAll();
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

        // Create director and admin users
        HospitalDirector director = testAuthUtils.createTestDirector("director@example.com", hospital);
        directorToken = testAuthUtils.generateToken("director@example.com", UserRole.DIRECTOR);
        
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

        // Create appointments
        Appointment appointment1 = new Appointment();
        appointment1.setPatient(patient);
        appointment1.setDoctor(doctor);
        appointment1.setHospital(hospital);
        appointment1.setAppointmentDateTime(LocalDateTime.now().plusHours(2));
        appointment1.setDurationMinutes(30);
        appointment1.setStatus(AppointmentStatus.SCHEDULED);
        appointment1.setType(AppointmentType.CONSULTATION);
        appointment1.setReason("Checkup");
        appointmentRepository.save(appointment1);

        Appointment appointment2 = new Appointment();
        appointment2.setPatient(patient);
        appointment2.setDoctor(doctor);
        appointment2.setHospital(hospital);
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
        medicalRecord.setHospital(hospital);
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
    void shouldGetDirectorDashboard() throws Exception {
        mockMvc.perform(get("/api/director/dashboard")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(directorToken)))
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
        mockMvc.perform(get("/api/director/doctors/performance")
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(directorToken)))
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
        mockMvc.perform(get("/api/director/doctors/{doctorId}/performance", doctor.getId())
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(directorToken)))
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
        mockMvc.perform(get("/api/director/doctors/{doctorId}/performance", 999L)
                        .header("Authorization", testAuthUtils.getAuthorizationHeader(directorToken)))
                .andExpect(status().isNotFound());
    }
}
