package com.hospital.management.repositories;

import com.hospital.management.entities.*;
import com.hospital.management.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 10.6: Hospital-Scoped Repository Tests
 * Tests all hospital-filtered repository methods to ensure proper data isolation
 */
@DataJpaTest
@ActiveProfiles("test")
class HospitalScopedRepositoryTest {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PharmacyStockRepository pharmacyStockRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    private Hospital hospital1;
    private Hospital hospital2;
    private Patient patient1;
    private Patient patient2;
    private Doctor doctor1;
    private Doctor doctor2;

    @BeforeEach
    void setUp() {
        // Create two hospitals
        hospital1 = new Hospital();
        hospital1.setName("City General Hospital");
        hospital1.setAddress("123 Main St");
        hospital1.setPhone("555-0001");
        hospital1.setEmail("info@citygeneral.com");
        hospital1.setRegistrationNumber("REG001");
        hospital1.setEstablishedDate(LocalDate.of(2000, 1, 1));
        hospital1 = hospitalRepository.save(hospital1);

        hospital2 = new Hospital();
        hospital2.setName("County Medical Center");
        hospital2.setAddress("456 Oak Ave");
        hospital2.setPhone("555-0002");
        hospital2.setEmail("info@countymedical.com");
        hospital2.setRegistrationNumber("REG002");
        hospital2.setEstablishedDate(LocalDate.of(2005, 1, 1));
        hospital2 = hospitalRepository.save(hospital2);

        // Create patients in different hospitals
        patient1 = new Patient();
        patient1.setFirstName("John");
        patient1.setLastName("Doe");
        patient1.setEmail("john.doe@example.com");
        patient1.setPhone("555-1001");
        patient1.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient1.setGender(Gender.MALE);
        patient1.setBloodType("A+");
        patient1.setHospital(hospital1);
        patient1 = patientRepository.save(patient1);

        patient2 = new Patient();
        patient2.setFirstName("Jane");
        patient2.setLastName("Smith");
        patient2.setEmail("jane.smith@example.com");
        patient2.setPhone("555-1002");
        patient2.setDateOfBirth(LocalDate.of(1985, 5, 15));
        patient2.setGender(Gender.FEMALE);
        patient2.setBloodType("B+");
        patient2.setHospital(hospital2);
        patient2 = patientRepository.save(patient2);

        // Create doctors in different hospitals
        doctor1 = new Doctor();
        doctor1.setFirstName("Dr. Alice");
        doctor1.setLastName("Johnson");
        doctor1.setEmail("alice.johnson@example.com");
        doctor1.setPhone("555-2001");
        doctor1.setSpecialization("Cardiology");
        doctor1.setLicenseNumber("LIC001");
        doctor1.setYearsOfExperience(10);
        doctor1.setQualification("MD");
        doctor1.setHospital(hospital1);
        doctor1 = doctorRepository.save(doctor1);

        doctor2 = new Doctor();
        doctor2.setFirstName("Dr. Bob");
        doctor2.setLastName("Williams");
        doctor2.setEmail("bob.williams@example.com");
        doctor2.setPhone("555-2002");
        doctor2.setSpecialization("Neurology");
        doctor2.setLicenseNumber("LIC002");
        doctor2.setYearsOfExperience(15);
        doctor2.setQualification("MD, PhD");
        doctor2.setHospital(hospital2);
        doctor2 = doctorRepository.save(doctor2);
    }

    // ==================== MedicalRecordRepository Tests ====================

    @Test
    void shouldFindMedicalRecordsByHospitalId() {
        // Given: Medical records in both hospitals
        MedicalRecord record1 = createMedicalRecord(patient1, doctor1, hospital1);
        MedicalRecord record2 = createMedicalRecord(patient2, doctor2, hospital2);
        medicalRecordRepository.save(record1);
        medicalRecordRepository.save(record2);

        // When: Find records by hospital1 ID
        List<MedicalRecord> hospital1Records = medicalRecordRepository.findByHospitalIdOrderByVisitDateDesc(hospital1.getId());

        // Then: Should only return hospital1 records
        assertThat(hospital1Records).hasSize(1);
        assertThat(hospital1Records.get(0).getHospital().getId()).isEqualTo(hospital1.getId());
    }

    @Test
    void shouldFindMedicalRecordsByDoctorIdAndHospitalId() {
        // Given: Medical records for doctor1 in hospital1
        MedicalRecord record1 = createMedicalRecord(patient1, doctor1, hospital1);
        MedicalRecord record2 = createMedicalRecord(patient2, doctor2, hospital2);
        medicalRecordRepository.save(record1);
        medicalRecordRepository.save(record2);

        // When: Find records by doctor1 and hospital1
        List<MedicalRecord> records = medicalRecordRepository.findByDoctorIdAndHospitalId(doctor1.getId(), hospital1.getId());

        // Then: Should only return doctor1's records in hospital1
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getDoctor().getId()).isEqualTo(doctor1.getId());
        assertThat(records.get(0).getHospital().getId()).isEqualTo(hospital1.getId());
    }

    @Test
    void shouldCountMedicalRecordsByHospitalId() {
        // Given: Multiple records in hospital1
        medicalRecordRepository.save(createMedicalRecord(patient1, doctor1, hospital1));
        medicalRecordRepository.save(createMedicalRecord(patient1, doctor1, hospital1));
        medicalRecordRepository.save(createMedicalRecord(patient2, doctor2, hospital2));

        // When: Count records in hospital1
        Long count = medicalRecordRepository.countByHospitalId(hospital1.getId());

        // Then: Should return 2
        assertThat(count).isEqualTo(2);
    }

    // ==================== AppointmentRepository Tests ====================

    @Test
    void shouldFindAppointmentsByHospitalId() {
        // Given: Appointments in both hospitals
        Appointment apt1 = createAppointment(patient1, doctor1, hospital1);
        Appointment apt2 = createAppointment(patient2, doctor2, hospital2);
        appointmentRepository.save(apt1);
        appointmentRepository.save(apt2);

        // When: Find appointments by hospital1 ID
        List<Appointment> hospital1Appointments = appointmentRepository.findByHospitalIdOrderByAppointmentDateTimeDesc(hospital1.getId());

        // Then: Should only return hospital1 appointments
        assertThat(hospital1Appointments).hasSize(1);
        assertThat(hospital1Appointments.get(0).getHospital().getId()).isEqualTo(hospital1.getId());
    }

    @Test
    void shouldFindAppointmentsByDoctorIdAndHospitalId() {
        // Given: Appointments for doctor1 in hospital1
        Appointment apt1 = createAppointment(patient1, doctor1, hospital1);
        Appointment apt2 = createAppointment(patient2, doctor2, hospital2);
        appointmentRepository.save(apt1);
        appointmentRepository.save(apt2);

        // When: Find appointments by doctor1 and hospital1
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndHospitalId(doctor1.getId(), hospital1.getId());

        // Then: Should only return doctor1's appointments in hospital1
        assertThat(appointments).hasSize(1);
        assertThat(appointments.get(0).getDoctor().getId()).isEqualTo(doctor1.getId());
        assertThat(appointments.get(0).getHospital().getId()).isEqualTo(hospital1.getId());
    }

    @Test
    void shouldCountAppointmentsByHospitalId() {
        // Given: Multiple appointments in hospital1
        appointmentRepository.save(createAppointment(patient1, doctor1, hospital1));
        appointmentRepository.save(createAppointment(patient1, doctor1, hospital1));
        appointmentRepository.save(createAppointment(patient2, doctor2, hospital2));

        // When: Count appointments in hospital1
        Long count = appointmentRepository.countByHospitalId(hospital1.getId());

        // Then: Should return 2
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldCountAppointmentsByHospitalIdAndStatus() {
        // Given: Appointments with different statuses in hospital1
        Appointment apt1 = createAppointment(patient1, doctor1, hospital1);
        apt1.setStatus(AppointmentStatus.SCHEDULED);
        Appointment apt2 = createAppointment(patient1, doctor1, hospital1);
        apt2.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(apt1);
        appointmentRepository.save(apt2);

        // When: Count scheduled appointments in hospital1
        Long count = appointmentRepository.countByHospitalIdAndStatus(hospital1.getId(), AppointmentStatus.SCHEDULED);

        // Then: Should return 1
        assertThat(count).isEqualTo(1);
    }

    // ==================== PrescriptionRepository Tests ====================

    @Test
    void shouldFindPrescriptionsByHospitalId() {
        // Given: Prescriptions in both hospitals
        Prescription presc1 = createPrescription(patient1, doctor1, hospital1);
        Prescription presc2 = createPrescription(patient2, doctor2, hospital2);
        prescriptionRepository.save(presc1);
        prescriptionRepository.save(presc2);

        // When: Find prescriptions by hospital1 ID
        List<Prescription> hospital1Prescriptions = prescriptionRepository.findByHospitalIdOrderByPrescribedDateDesc(hospital1.getId());

        // Then: Should only return hospital1 prescriptions
        assertThat(hospital1Prescriptions).hasSize(1);
        assertThat(hospital1Prescriptions.get(0).getHospital().getId()).isEqualTo(hospital1.getId());
    }

    @Test
    void shouldFindPrescriptionsByDoctorIdAndHospitalId() {
        // Given: Prescriptions for doctor1 in hospital1
        Prescription presc1 = createPrescription(patient1, doctor1, hospital1);
        Prescription presc2 = createPrescription(patient2, doctor2, hospital2);
        prescriptionRepository.save(presc1);
        prescriptionRepository.save(presc2);

        // When: Find prescriptions by doctor1 and hospital1
        List<Prescription> prescriptions = prescriptionRepository.findByDoctorIdAndHospitalId(doctor1.getId(), hospital1.getId());

        // Then: Should only return doctor1's prescriptions in hospital1
        assertThat(prescriptions).hasSize(1);
        assertThat(prescriptions.get(0).getDoctor().getId()).isEqualTo(doctor1.getId());
        assertThat(prescriptions.get(0).getHospital().getId()).isEqualTo(hospital1.getId());
    }

    @Test
    void shouldCountPrescriptionsByHospitalId() {
        // Given: Multiple prescriptions in hospital1
        prescriptionRepository.save(createPrescription(patient1, doctor1, hospital1));
        prescriptionRepository.save(createPrescription(patient1, doctor1, hospital1));
        prescriptionRepository.save(createPrescription(patient2, doctor2, hospital2));

        // When: Count prescriptions in hospital1
        Long count = prescriptionRepository.countByHospitalId(hospital1.getId());

        // Then: Should return 2
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldCountPrescriptionsByHospitalIdAndStatus() {
        // Given: Prescriptions with different statuses in hospital1
        Prescription presc1 = createPrescription(patient1, doctor1, hospital1);
        presc1.setStatus(PrescriptionStatus.ACTIVE);
        Prescription presc2 = createPrescription(patient1, doctor1, hospital1);
        presc2.setStatus(PrescriptionStatus.DISPENSED);
        prescriptionRepository.save(presc1);
        prescriptionRepository.save(presc2);

        // When: Count active prescriptions in hospital1
        Long count = prescriptionRepository.countByHospitalIdAndStatus(hospital1.getId(), PrescriptionStatus.ACTIVE);

        // Then: Should return 1
        assertThat(count).isEqualTo(1);
    }

    // ==================== PharmacyStockRepository Tests ====================

    @Test
    void shouldFindPharmacyStockByHospitalId() {
        // Given: Pharmacy stock in both hospitals
        Medication medication = createMedication();
        medicationRepository.save(medication);

        PharmacyStock stock1 = createPharmacyStock(medication, hospital1, 100);
        PharmacyStock stock2 = createPharmacyStock(medication, hospital2, 50);
        pharmacyStockRepository.save(stock1);
        pharmacyStockRepository.save(stock2);

        // When: Find stock by hospital1 ID
        List<PharmacyStock> hospital1Stock = pharmacyStockRepository.findByHospitalId(hospital1.getId());

        // Then: Should only return hospital1 stock
        assertThat(hospital1Stock).hasSize(1);
        assertThat(hospital1Stock.get(0).getHospital().getId()).isEqualTo(hospital1.getId());
    }

    @Test
    void shouldFindLowStockByHospitalId() {
        // Given: Low stock items in hospital1
        Medication medication = createMedication();
        medicationRepository.save(medication);

        PharmacyStock lowStock = createPharmacyStock(medication, hospital1, 5);
        lowStock.setReorderLevel(10);
        PharmacyStock normalStock = createPharmacyStock(medication, hospital1, 100);
        normalStock.setReorderLevel(10);
        pharmacyStockRepository.save(lowStock);
        pharmacyStockRepository.save(normalStock);

        // When: Find low stock in hospital1
        List<PharmacyStock> lowStockItems = pharmacyStockRepository.findLowStockByHospitalId(hospital1.getId());

        // Then: Should only return low stock items
        assertThat(lowStockItems).hasSize(1);
        assertThat(lowStockItems.get(0).getQuantity()).isLessThanOrEqualTo(lowStockItems.get(0).getReorderLevel());
    }

    @Test
    void shouldFindExpiringStockByHospitalId() {
        // Given: Expiring stock in hospital1
        Medication medication = createMedication();
        medicationRepository.save(medication);

        LocalDate soonDate = LocalDate.now().plusDays(10);
        LocalDate farDate = LocalDate.now().plusYears(1);

        PharmacyStock expiringSoon = createPharmacyStock(medication, hospital1, 50);
        expiringSoon.setExpiryDate(soonDate);
        PharmacyStock notExpiring = createPharmacyStock(medication, hospital1, 100);
        notExpiring.setExpiryDate(farDate);
        pharmacyStockRepository.save(expiringSoon);
        pharmacyStockRepository.save(notExpiring);

        // When: Find expiring stock in hospital1 (within 30 days)
        LocalDate checkDate = LocalDate.now().plusDays(30);
        List<PharmacyStock> expiringItems = pharmacyStockRepository.findExpiringByHospitalId(hospital1.getId(), checkDate);

        // Then: Should only return expiring items
        assertThat(expiringItems).hasSize(1);
        assertThat(expiringItems.get(0).getExpiryDate()).isBefore(checkDate);
    }

    @Test
    void shouldCountPharmacyStockByHospitalId() {
        // Given: Multiple stock items in hospital1
        Medication medication = createMedication();
        medicationRepository.save(medication);

        pharmacyStockRepository.save(createPharmacyStock(medication, hospital1, 100));
        pharmacyStockRepository.save(createPharmacyStock(medication, hospital1, 50));
        pharmacyStockRepository.save(createPharmacyStock(medication, hospital2, 75));

        // When: Count stock items in hospital1
        Long count = pharmacyStockRepository.countByHospitalId(hospital1.getId());

        // Then: Should return 2
        assertThat(count).isEqualTo(2);
    }

    // ==================== Helper Methods ====================

    private MedicalRecord createMedicalRecord(Patient patient, Doctor doctor, Hospital hospital) {
        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setHospital(hospital);
        record.setVisitDate(LocalDateTime.now());
        record.setChiefComplaint("Test complaint");
        record.setDiagnosis("Test diagnosis");
        record.setTreatment("Test treatment");
        record.setNotes("Test notes");
        record.setVitalSigns("BP: 120/80");
        return record;
    }

    private Appointment createAppointment(Patient patient, Doctor doctor, Hospital hospital) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setHospital(hospital);
        appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        appointment.setDurationMinutes(30);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setType(AppointmentType.CONSULTATION);
        appointment.setReason("Test appointment");
        return appointment;
    }

    private Prescription createPrescription(Patient patient, Doctor doctor, Hospital hospital) {
        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setHospital(hospital);
        prescription.setPrescribedDate(LocalDateTime.now());
        prescription.setValidUntil(LocalDate.now().plusDays(30));
        prescription.setStatus(PrescriptionStatus.ACTIVE);
        prescription.setMedicationName("Test Medication");
        prescription.setDosage("10mg");
        prescription.setFrequency("Twice daily");
        prescription.setDurationDays(30);
        return prescription;
    }

    private PharmacyStock createPharmacyStock(Medication medication, Hospital hospital, int quantity) {
        PharmacyStock stock = new PharmacyStock();
        stock.setMedication(medication);
        stock.setHospital(hospital);
        stock.setQuantity(quantity);
        stock.setReorderLevel(10);
        stock.setExpiryDate(LocalDate.now().plusYears(1));
        stock.setBatchNumber("BATCH-" + System.currentTimeMillis());
        stock.setUnitPrice(10.0);
        return stock;
    }

    private Medication createMedication() {
        Medication medication = new Medication();
        medication.setName("Test Medication");
        medication.setGenericName("Generic Test");
        medication.setType(MedicationType.TABLET);
        medication.setManufacturer("Test Pharma");
        medication.setDescription("Test description");
        return medication;
    }
}
