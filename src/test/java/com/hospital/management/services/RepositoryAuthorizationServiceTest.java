package com.hospital.management.services;

import com.hospital.management.entities.*;
import com.hospital.management.enums.UserRole;
import com.hospital.management.exceptions.UnauthorizedException;
import com.hospital.management.repositories.*;
import com.hospital.management.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Repository Authorization Service Tests
 * 
 * Tests for fine-grained authorization checks at the repository layer.
 * Verifies that authorization rules are enforced correctly for all entity types.
 * 
 * Phase 10.7: Repository Authorization
 */
@ExtendWith(MockitoExtension.class)
class RepositoryAuthorizationServiceTest {
    
    @Mock
    private SecurityUtils securityUtils;
    
    @Mock
    private HospitalRepository hospitalRepository;
    
    @Mock
    private DoctorRepository doctorRepository;
    
    @Mock
    private PatientRepository patientRepository;
    
    @Mock
    private MedicalRecordRepository medicalRecordRepository;
    
    @Mock
    private AppointmentRepository appointmentRepository;
    
    @Mock
    private PrescriptionRepository prescriptionRepository;
    
    @Mock
    private PharmacyStockRepository pharmacyStockRepository;
    
    private RepositoryAuthorizationService authorizationService;
    
    private Hospital hospital1;
    private Hospital hospital2;
    private Doctor doctor1;
    private Doctor doctor2;
    private Patient patient1;
    private Patient patient2;
    private MedicalRecord medicalRecord1;
    private Appointment appointment1;
    private Prescription prescription1;
    private PharmacyStock pharmacyStock1;
    
    @BeforeEach
    void setUp() {
        authorizationService = new RepositoryAuthorizationService(
                securityUtils,
                hospitalRepository,
                doctorRepository,
                patientRepository,
                medicalRecordRepository,
                appointmentRepository,
                prescriptionRepository,
                pharmacyStockRepository
        );
        
        // Create test hospitals
        hospital1 = new Hospital();
        hospital1.setId(1L);
        hospital1.setName("Hospital 1");
        hospital1.setRegistrationNumber("REG-001");
        
        hospital2 = new Hospital();
        hospital2.setId(2L);
        hospital2.setName("Hospital 2");
        hospital2.setRegistrationNumber("REG-002");
        
        // Create test doctors
        doctor1 = new Doctor();
        doctor1.setId(1L);
        doctor1.setEmail("doctor1@hospital.com");
        doctor1.setHospital(hospital1);
        
        doctor2 = new Doctor();
        doctor2.setId(2L);
        doctor2.setEmail("doctor2@hospital.com");
        doctor2.setHospital(hospital2);
        
        // Create test patients
        patient1 = new Patient();
        patient1.setId(1L);
        patient1.setEmail("patient1@hospital.com");
        patient1.setHospital(hospital1);
        
        patient2 = new Patient();
        patient2.setId(2L);
        patient2.setEmail("patient2@hospital.com");
        patient2.setHospital(hospital2);
        
        // Create test medical record
        medicalRecord1 = new MedicalRecord();
        medicalRecord1.setId(1L);
        medicalRecord1.setDoctor(doctor1);
        medicalRecord1.setPatient(patient1);
        medicalRecord1.setHospital(hospital1);
        
        // Create test appointment
        appointment1 = new Appointment();
        appointment1.setId(1L);
        appointment1.setDoctor(doctor1);
        appointment1.setPatient(patient1);
        appointment1.setHospital(hospital1);
        
        // Create test prescription
        prescription1 = new Prescription();
        prescription1.setId(1L);
        prescription1.setDoctor(doctor1);
        prescription1.setPatient(patient1);
        prescription1.setHospital(hospital1);
        
        // Create test pharmacy stock
        pharmacyStock1 = new PharmacyStock();
        pharmacyStock1.setId(1L);
        pharmacyStock1.setHospital(hospital1);
    }
    
    // ==================== Hospital Access Tests ====================
    
    @Test
    void shouldAllowAdminAccessToAnyHospital() {
        // Given: Admin user
        when(securityUtils.isAdmin()).thenReturn(true);
        
        // When: Admin accesses hospital 1
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyHospitalAccess(1L));
    }
    
    @Test
    void shouldAllowDirectorAccessToOwnHospital() {
        // Given: Director in hospital 1
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserHospitalId()).thenReturn(1L);
        
        // When: Director accesses hospital 1
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyHospitalAccess(1L));
    }
    
    @Test
    void shouldDenyDirectorAccessToOtherHospital() {
        // Given: Director in hospital 1
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserHospitalId()).thenReturn(1L);
        
        // When: Director tries to access hospital 2
        // Then: Should throw UnauthorizedException
        assertThrows(UnauthorizedException.class, 
                () -> authorizationService.verifyHospitalAccess(2L));
    }
    
    // ==================== Doctor Access Tests ====================
    
    @Test
    void shouldAllowAdminAccessToAnyDoctor() {
        // Given: Admin user
        when(securityUtils.isAdmin()).thenReturn(true);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor1));
        
        // When: Admin accesses doctor 1
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyDoctorAccess(1L));
    }
    
    @Test
    void shouldAllowDirectorAccessToDoctorInOwnHospital() {
        // Given: Director in hospital 1
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserHospitalId()).thenReturn(1L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor1));
        
        // When: Director accesses doctor 1 (in hospital 1)
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyDoctorAccess(1L));
    }
    
    @Test
    void shouldDenyDirectorAccessToDoctorInOtherHospital() {
        // Given: Director in hospital 1
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserHospitalId()).thenReturn(1L);
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor2));
        
        // When: Director tries to access doctor 2 (in hospital 2)
        // Then: Should throw UnauthorizedException
        assertThrows(UnauthorizedException.class, 
                () -> authorizationService.verifyDoctorAccess(2L));
    }
    
    @Test
    void shouldThrowExceptionWhenDoctorNotFound() {
        // Given: Doctor does not exist
        when(securityUtils.isAdmin()).thenReturn(false);
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When: Trying to verify access to non-existent doctor
        // Then: Should throw UnauthorizedException
        assertThrows(UnauthorizedException.class, 
                () -> authorizationService.verifyDoctorAccess(999L));
    }
    
    // ==================== Patient Access Tests ====================
    
    @Test
    void shouldAllowAdminAccessToAnyPatient() {
        // Given: Admin user
        when(securityUtils.isAdmin()).thenReturn(true);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient1));
        
        // When: Admin accesses patient 1
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyPatientAccess(1L));
    }
    
    @Test
    void shouldAllowPatientAccessToOwnData() {
        // Given: Patient 1 logged in
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.PATIENT);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient1));
        
        // When: Patient accesses own data
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyPatientAccess(1L));
    }
    
    @Test
    void shouldDenyPatientAccessToOtherPatientData() {
        // Given: Patient 1 logged in
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.PATIENT);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient2));
        
        // When: Patient tries to access other patient's data
        // Then: Should throw UnauthorizedException
        assertThrows(UnauthorizedException.class, 
                () -> authorizationService.verifyPatientAccess(2L));
    }
    
    @Test
    void shouldAllowDoctorAccessToPatientInOwnHospital() {
        // Given: Doctor in hospital 1
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.DOCTOR);
        when(securityUtils.getCurrentUserHospitalId()).thenReturn(1L);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient1));
        
        // When: Doctor accesses patient in own hospital
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyPatientAccess(1L));
    }
    
    @Test
    void shouldDenyDoctorAccessToPatientInOtherHospital() {
        // Given: Doctor in hospital 1
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.DOCTOR);
        when(securityUtils.getCurrentUserHospitalId()).thenReturn(1L);
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient2));
        
        // When: Doctor tries to access patient in other hospital
        // Then: Should throw UnauthorizedException
        assertThrows(UnauthorizedException.class, 
                () -> authorizationService.verifyPatientAccess(2L));
    }
    
    // ==================== Doctor-Patient Relationship Tests ====================
    
    @Test
    void shouldAllowAdminAccessToDoctorPatientRelationship() {
        // Given: Admin user
        when(securityUtils.isAdmin()).thenReturn(true);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor1));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient1));
        
        // When: Admin verifies doctor-patient relationship
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyDoctorPatientAccess(1L, 1L));
    }
    
    @Test
    void shouldAllowDoctorPatientInSameHospital() {
        // Given: Doctor and patient in same hospital
        when(securityUtils.isAdmin()).thenReturn(false);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor1));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient1));
        
        // When: Verifying doctor-patient relationship
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyDoctorPatientAccess(1L, 1L));
    }
    
    @Test
    void shouldDenyDoctorPatientInDifferentHospitals() {
        // Given: Doctor in hospital 1, patient in hospital 2
        when(securityUtils.isAdmin()).thenReturn(false);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor1));
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient2));
        
        // When: Trying to verify doctor-patient relationship across hospitals
        // Then: Should throw UnauthorizedException
        assertThrows(UnauthorizedException.class, 
                () -> authorizationService.verifyDoctorPatientAccess(1L, 2L));
    }
    
    // ==================== Medical Record Access Tests ====================
    
    @Test
    void shouldAllowAdminAccessToAnyMedicalRecord() {
        // Given: Admin user
        when(securityUtils.isAdmin()).thenReturn(true);
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(medicalRecord1));
        
        // When: Admin accesses medical record
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyMedicalRecordAccess(1L));
    }
    
    @Test
    void shouldAllowPatientAccessToOwnMedicalRecord() {
        // Given: Patient 1 logged in
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.PATIENT);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(medicalRecord1));
        
        // When: Patient accesses own medical record
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyMedicalRecordAccess(1L));
    }
    
    @Test
    void shouldDenyPatientAccessToOtherPatientMedicalRecord() {
        // Given: Patient 1 logged in, record belongs to patient 2
        MedicalRecord record2 = new MedicalRecord();
        record2.setId(2L);
        record2.setDoctor(doctor1);
        record2.setPatient(patient2);
        record2.setHospital(hospital1);
        
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.PATIENT);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(medicalRecordRepository.findById(2L)).thenReturn(Optional.of(record2));
        
        // When: Patient tries to access other patient's record
        // Then: Should throw UnauthorizedException
        assertThrows(UnauthorizedException.class, 
                () -> authorizationService.verifyMedicalRecordAccess(2L));
    }
    
    @Test
    void shouldAllowDoctorAccessToOwnMedicalRecord() {
        // Given: Doctor 1 logged in
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.DOCTOR);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(medicalRecord1));
        
        // When: Doctor accesses own medical record
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyMedicalRecordAccess(1L));
    }
    
    @Test
    void shouldDenyDoctorAccessToOtherDoctorMedicalRecord() {
        // Given: Doctor 1 logged in, record belongs to doctor 2
        MedicalRecord record2 = new MedicalRecord();
        record2.setId(2L);
        record2.setDoctor(doctor2);
        record2.setPatient(patient2);
        record2.setHospital(hospital2);
        
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.DOCTOR);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(medicalRecordRepository.findById(2L)).thenReturn(Optional.of(record2));
        
        // When: Doctor tries to access other doctor's record
        // Then: Should throw UnauthorizedException
        assertThrows(UnauthorizedException.class, 
                () -> authorizationService.verifyMedicalRecordAccess(2L));
    }
    
    @Test
    void shouldAllowDirectorAccessToHospitalMedicalRecord() {
        // Given: Director in hospital 1
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.DIRECTOR);
        when(securityUtils.getCurrentUserHospitalId()).thenReturn(1L);
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(medicalRecord1));
        
        // When: Director accesses hospital's medical record
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyMedicalRecordAccess(1L));
    }
    
    @Test
    void shouldDenyDirectorAccessToOtherHospitalMedicalRecord() {
        // Given: Director in hospital 1, record in hospital 2
        MedicalRecord record2 = new MedicalRecord();
        record2.setId(2L);
        record2.setDoctor(doctor2);
        record2.setPatient(patient2);
        record2.setHospital(hospital2);
        
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.DIRECTOR);
        when(securityUtils.getCurrentUserHospitalId()).thenReturn(1L);
        when(medicalRecordRepository.findById(2L)).thenReturn(Optional.of(record2));
        
        // When: Director tries to access other hospital's record
        // Then: Should throw UnauthorizedException
        assertThrows(UnauthorizedException.class, 
                () -> authorizationService.verifyMedicalRecordAccess(2L));
    }
    
    // ==================== Appointment Access Tests ====================
    
    @Test
    void shouldAllowAdminAccessToAnyAppointment() {
        // Given: Admin user
        when(securityUtils.isAdmin()).thenReturn(true);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment1));
        
        // When: Admin accesses appointment
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyAppointmentAccess(1L));
    }
    
    @Test
    void shouldAllowPatientAccessToOwnAppointment() {
        // Given: Patient 1 logged in
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.PATIENT);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment1));
        
        // When: Patient accesses own appointment
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyAppointmentAccess(1L));
    }
    
    @Test
    void shouldAllowDoctorAccessToOwnAppointment() {
        // Given: Doctor 1 logged in
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.DOCTOR);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment1));
        
        // When: Doctor accesses own appointment
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyAppointmentAccess(1L));
    }
    
    @Test
    void shouldAllowDirectorAccessToHospitalAppointment() {
        // Given: Director in hospital 1
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.DIRECTOR);
        when(securityUtils.getCurrentUserHospitalId()).thenReturn(1L);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment1));
        
        // When: Director accesses hospital's appointment
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyAppointmentAccess(1L));
    }
    
    // ==================== Prescription Access Tests ====================
    
    @Test
    void shouldAllowAdminAccessToAnyPrescription() {
        // Given: Admin user
        when(securityUtils.isAdmin()).thenReturn(true);
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription1));
        
        // When: Admin accesses prescription
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyPrescriptionAccess(1L));
    }
    
    @Test
    void shouldAllowPatientAccessToOwnPrescription() {
        // Given: Patient 1 logged in
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.PATIENT);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription1));
        
        // When: Patient accesses own prescription
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyPrescriptionAccess(1L));
    }
    
    @Test
    void shouldAllowDoctorAccessToOwnPrescription() {
        // Given: Doctor 1 logged in
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.DOCTOR);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription1));
        
        // When: Doctor accesses own prescription
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyPrescriptionAccess(1L));
    }
    
    @Test
    void shouldAllowPharmacistAccessToHospitalPrescription() {
        // Given: Pharmacist in hospital 1
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.PHARMACIST);
        when(securityUtils.getCurrentUserHospitalId()).thenReturn(1L);
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription1));
        
        // When: Pharmacist accesses hospital's prescription
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyPrescriptionAccess(1L));
    }
    
    @Test
    void shouldDenyPharmacistAccessToOtherHospitalPrescription() {
        // Given: Pharmacist in hospital 1, prescription in hospital 2
        Prescription prescription2 = new Prescription();
        prescription2.setId(2L);
        prescription2.setDoctor(doctor2);
        prescription2.setPatient(patient2);
        prescription2.setHospital(hospital2);
        
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.PHARMACIST);
        when(securityUtils.getCurrentUserHospitalId()).thenReturn(1L);
        when(prescriptionRepository.findById(2L)).thenReturn(Optional.of(prescription2));
        
        // When: Pharmacist tries to access other hospital's prescription
        // Then: Should throw UnauthorizedException
        assertThrows(UnauthorizedException.class, 
                () -> authorizationService.verifyPrescriptionAccess(2L));
    }
    
    // ==================== Pharmacy Stock Access Tests ====================
    
    @Test
    void shouldAllowAdminAccessToAnyPharmacyStock() {
        // Given: Admin user
        when(securityUtils.isAdmin()).thenReturn(true);
        when(pharmacyStockRepository.findById(1L)).thenReturn(Optional.of(pharmacyStock1));
        
        // When: Admin accesses pharmacy stock
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyPharmacyStockAccess(1L));
    }
    
    @Test
    void shouldAllowPharmacistAccessToOwnHospitalStock() {
        // Given: Pharmacist in hospital 1
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.PHARMACIST);
        when(securityUtils.getCurrentUserHospitalId()).thenReturn(1L);
        when(pharmacyStockRepository.findById(1L)).thenReturn(Optional.of(pharmacyStock1));
        
        // When: Pharmacist accesses own hospital's stock
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyPharmacyStockAccess(1L));
    }
    
    @Test
    void shouldDenyPharmacistAccessToOtherHospitalStock() {
        // Given: Pharmacist in hospital 1, stock in hospital 2
        PharmacyStock stock2 = new PharmacyStock();
        stock2.setId(2L);
        stock2.setHospital(hospital2);
        
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.PHARMACIST);
        when(securityUtils.getCurrentUserHospitalId()).thenReturn(1L);
        when(pharmacyStockRepository.findById(2L)).thenReturn(Optional.of(stock2));
        
        // When: Pharmacist tries to access other hospital's stock
        // Then: Should throw UnauthorizedException
        assertThrows(UnauthorizedException.class, 
                () -> authorizationService.verifyPharmacyStockAccess(2L));
    }
    
    @Test
    void shouldAllowDirectorAccessToOwnHospitalStock() {
        // Given: Director in hospital 1
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.DIRECTOR);
        when(securityUtils.getCurrentUserHospitalId()).thenReturn(1L);
        when(pharmacyStockRepository.findById(1L)).thenReturn(Optional.of(pharmacyStock1));
        
        // When: Director accesses own hospital's stock
        // Then: Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyPharmacyStockAccess(1L));
    }
    
    @Test
    void shouldDenyPatientAccessToPharmacyStock() {
        // Given: Patient logged in
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.PATIENT);
        when(pharmacyStockRepository.findById(1L)).thenReturn(Optional.of(pharmacyStock1));
        
        // When: Patient tries to access pharmacy stock
        // Then: Should throw UnauthorizedException
        assertThrows(UnauthorizedException.class, 
                () -> authorizationService.verifyPharmacyStockAccess(1L));
    }
    
    @Test
    void shouldDenyDoctorAccessToPharmacyStock() {
        // Given: Doctor logged in
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserRole()).thenReturn(UserRole.DOCTOR);
        when(pharmacyStockRepository.findById(1L)).thenReturn(Optional.of(pharmacyStock1));
        
        // When: Doctor tries to access pharmacy stock
        // Then: Should throw UnauthorizedException
        assertThrows(UnauthorizedException.class, 
                () -> authorizationService.verifyPharmacyStockAccess(1L));
    }
}
