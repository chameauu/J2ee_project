package com.hospital.management.services;

import com.hospital.management.entities.*;
import com.hospital.management.enums.UserRole;
import com.hospital.management.exceptions.UnauthorizedException;
import com.hospital.management.repositories.*;
import com.hospital.management.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Repository Authorization Service
 * 
 * Provides fine-grained authorization checks for repository operations.
 * Enforces hospital boundaries and role-based access control at the repository layer.
 * 
 * Authorization Rules:
 * - ADMIN: Can access any hospital and any entity
 * - DIRECTOR: Can access only their hospital's data
 * - DOCTOR: Can access only their patients' data and their hospital's data
 * - PHARMACIST: Can access only their hospital's pharmacy data
 * - PATIENT: Can access only their own data
 * 
 * Phase 10.7: Repository Authorization
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryAuthorizationService {
    
    private final SecurityUtils securityUtils;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PharmacyStockRepository pharmacyStockRepository;
    
    // ==================== Hospital Access ====================
    
    /**
     * Verify if current user can access a hospital
     * 
     * @param hospitalId Hospital ID to verify access
     * @throws UnauthorizedException if user cannot access hospital
     */
    public void verifyHospitalAccess(Long hospitalId) {
        // ADMIN can access any hospital
        if (securityUtils.isAdmin()) {
            logAuthorizationCheck("HOSPITAL_ACCESS", hospitalId, true);
            return;
        }
        
        // Get current user's hospital
        Long userHospitalId = securityUtils.getCurrentUserHospitalId();
        
        // User can only access their own hospital
        if (!userHospitalId.equals(hospitalId)) {
            logAuthorizationDenial("HOSPITAL_ACCESS", hospitalId);
            throw new UnauthorizedException(
                    "Access denied to hospital: " + hospitalId + 
                    ". User is assigned to hospital: " + userHospitalId
            );
        }
        
        logAuthorizationCheck("HOSPITAL_ACCESS", hospitalId, true);
    }
    
    // ==================== Doctor Access ====================
    
    /**
     * Verify if current user can access a doctor's data
     * 
     * @param doctorId Doctor ID to verify access
     * @throws UnauthorizedException if user cannot access doctor
     */
    public void verifyDoctorAccess(Long doctorId) {
        // ADMIN can access any doctor
        if (securityUtils.isAdmin()) {
            logAuthorizationCheck("DOCTOR_ACCESS", doctorId, true);
            return;
        }
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new UnauthorizedException("Doctor not found: " + doctorId));
        
        // Verify user's hospital matches doctor's hospital
        Long userHospitalId = securityUtils.getCurrentUserHospitalId();
        if (!userHospitalId.equals(doctor.getHospital().getId())) {
            logAuthorizationDenial("DOCTOR_ACCESS", doctorId);
            throw new UnauthorizedException(
                    "Access denied to doctor: " + doctorId + 
                    ". Doctor is in hospital: " + doctor.getHospital().getId() +
                    ". User is in hospital: " + userHospitalId
            );
        }
        
        logAuthorizationCheck("DOCTOR_ACCESS", doctorId, true);
    }
    
    // ==================== Patient Access ====================
    
    /**
     * Verify if current user can access a patient's data
     * 
     * @param patientId Patient ID to verify access
     * @throws UnauthorizedException if user cannot access patient
     */
    public void verifyPatientAccess(Long patientId) {
        // ADMIN can access any patient
        if (securityUtils.isAdmin()) {
            logAuthorizationCheck("PATIENT_ACCESS", patientId, true);
            return;
        }
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new UnauthorizedException("Patient not found: " + patientId));
        
        UserRole currentRole = securityUtils.getCurrentUserRole();
        Long currentUserId = securityUtils.getCurrentUserId();
        
        // PATIENT can only access their own data
        if (currentRole == UserRole.PATIENT) {
            if (!currentUserId.equals(patientId)) {
                logAuthorizationDenial("PATIENT_ACCESS", patientId);
                throw new UnauthorizedException(
                        "Access denied to patient: " + patientId + 
                        ". Patients can only access their own data."
                );
            }
            logAuthorizationCheck("PATIENT_ACCESS", patientId, true);
            return;
        }
        
        // DOCTOR, PHARMACIST, DIRECTOR can access patients in their hospital
        Long userHospitalId = securityUtils.getCurrentUserHospitalId();
        if (!userHospitalId.equals(patient.getHospital().getId())) {
            logAuthorizationDenial("PATIENT_ACCESS", patientId);
            throw new UnauthorizedException(
                    "Access denied to patient: " + patientId + 
                    ". Patient is in hospital: " + patient.getHospital().getId() +
                    ". User is in hospital: " + userHospitalId
            );
        }
        
        logAuthorizationCheck("PATIENT_ACCESS", patientId, true);
    }
    
    // ==================== Doctor-Patient Relationship ====================
    
    /**
     * Verify if a doctor can access a patient's data
     * 
     * @param doctorId Doctor ID
     * @param patientId Patient ID
     * @throws UnauthorizedException if doctor cannot access patient
     */
    public void verifyDoctorPatientAccess(Long doctorId, Long patientId) {
        // ADMIN can access any relationship
        if (securityUtils.isAdmin()) {
            logAuthorizationCheck("DOCTOR_PATIENT_ACCESS", doctorId + ":" + patientId, true);
            return;
        }
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new UnauthorizedException("Doctor not found: " + doctorId));
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new UnauthorizedException("Patient not found: " + patientId));
        
        // Verify doctor and patient are in same hospital
        if (!doctor.getHospital().getId().equals(patient.getHospital().getId())) {
            logAuthorizationDenial("DOCTOR_PATIENT_ACCESS", doctorId + ":" + patientId);
            throw new UnauthorizedException(
                    "Access denied. Doctor and patient are in different hospitals. " +
                    "Doctor hospital: " + doctor.getHospital().getId() +
                    ", Patient hospital: " + patient.getHospital().getId()
            );
        }
        
        logAuthorizationCheck("DOCTOR_PATIENT_ACCESS", doctorId + ":" + patientId, true);
    }
    
    // ==================== Medical Record Access ====================
    
    /**
     * Verify if current user can access a medical record
     * 
     * @param recordId Medical record ID to verify access
     * @throws UnauthorizedException if user cannot access record
     */
    public void verifyMedicalRecordAccess(Long recordId) {
        // ADMIN can access any record
        if (securityUtils.isAdmin()) {
            logAuthorizationCheck("MEDICAL_RECORD_ACCESS", recordId, true);
            return;
        }
        
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new UnauthorizedException("Medical record not found: " + recordId));
        
        UserRole currentRole = securityUtils.getCurrentUserRole();
        Long currentUserId = securityUtils.getCurrentUserId();
        
        // PATIENT can only access their own records
        if (currentRole == UserRole.PATIENT) {
            if (!currentUserId.equals(record.getPatient().getId())) {
                logAuthorizationDenial("MEDICAL_RECORD_ACCESS", recordId);
                throw new UnauthorizedException(
                        "Access denied to medical record: " + recordId + 
                        ". Patients can only access their own records."
                );
            }
            logAuthorizationCheck("MEDICAL_RECORD_ACCESS", recordId, true);
            return;
        }
        
        // DOCTOR can only access records for their patients
        if (currentRole == UserRole.DOCTOR) {
            if (!currentUserId.equals(record.getDoctor().getId())) {
                logAuthorizationDenial("MEDICAL_RECORD_ACCESS", recordId);
                throw new UnauthorizedException(
                        "Access denied to medical record: " + recordId + 
                        ". Doctors can only access records for their own patients."
                );
            }
            logAuthorizationCheck("MEDICAL_RECORD_ACCESS", recordId, true);
            return;
        }
        
        // DIRECTOR, PHARMACIST can access records in their hospital
        Long userHospitalId = securityUtils.getCurrentUserHospitalId();
        if (!userHospitalId.equals(record.getHospital().getId())) {
            logAuthorizationDenial("MEDICAL_RECORD_ACCESS", recordId);
            throw new UnauthorizedException(
                    "Access denied to medical record: " + recordId + 
                    ". Record is in hospital: " + record.getHospital().getId() +
                    ". User is in hospital: " + userHospitalId
            );
        }
        
        logAuthorizationCheck("MEDICAL_RECORD_ACCESS", recordId, true);
    }
    
    // ==================== Appointment Access ====================
    
    /**
     * Verify if current user can access an appointment
     * 
     * @param appointmentId Appointment ID to verify access
     * @throws UnauthorizedException if user cannot access appointment
     */
    public void verifyAppointmentAccess(Long appointmentId) {
        // ADMIN can access any appointment
        if (securityUtils.isAdmin()) {
            logAuthorizationCheck("APPOINTMENT_ACCESS", appointmentId, true);
            return;
        }
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new UnauthorizedException("Appointment not found: " + appointmentId));
        
        UserRole currentRole = securityUtils.getCurrentUserRole();
        Long currentUserId = securityUtils.getCurrentUserId();
        
        // PATIENT can only access their own appointments
        if (currentRole == UserRole.PATIENT) {
            if (!currentUserId.equals(appointment.getPatient().getId())) {
                logAuthorizationDenial("APPOINTMENT_ACCESS", appointmentId);
                throw new UnauthorizedException(
                        "Access denied to appointment: " + appointmentId + 
                        ". Patients can only access their own appointments."
                );
            }
            logAuthorizationCheck("APPOINTMENT_ACCESS", appointmentId, true);
            return;
        }
        
        // DOCTOR can only access their own appointments
        if (currentRole == UserRole.DOCTOR) {
            if (!currentUserId.equals(appointment.getDoctor().getId())) {
                logAuthorizationDenial("APPOINTMENT_ACCESS", appointmentId);
                throw new UnauthorizedException(
                        "Access denied to appointment: " + appointmentId + 
                        ". Doctors can only access their own appointments."
                );
            }
            logAuthorizationCheck("APPOINTMENT_ACCESS", appointmentId, true);
            return;
        }
        
        // DIRECTOR, PHARMACIST can access appointments in their hospital
        Long userHospitalId = securityUtils.getCurrentUserHospitalId();
        if (!userHospitalId.equals(appointment.getHospital().getId())) {
            logAuthorizationDenial("APPOINTMENT_ACCESS", appointmentId);
            throw new UnauthorizedException(
                    "Access denied to appointment: " + appointmentId + 
                    ". Appointment is in hospital: " + appointment.getHospital().getId() +
                    ". User is in hospital: " + userHospitalId
            );
        }
        
        logAuthorizationCheck("APPOINTMENT_ACCESS", appointmentId, true);
    }
    
    // ==================== Prescription Access ====================
    
    /**
     * Verify if current user can access a prescription
     * 
     * @param prescriptionId Prescription ID to verify access
     * @throws UnauthorizedException if user cannot access prescription
     */
    public void verifyPrescriptionAccess(Long prescriptionId) {
        // ADMIN can access any prescription
        if (securityUtils.isAdmin()) {
            logAuthorizationCheck("PRESCRIPTION_ACCESS", prescriptionId, true);
            return;
        }
        
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new UnauthorizedException("Prescription not found: " + prescriptionId));
        
        UserRole currentRole = securityUtils.getCurrentUserRole();
        Long currentUserId = securityUtils.getCurrentUserId();
        
        // PATIENT can only access their own prescriptions
        if (currentRole == UserRole.PATIENT) {
            if (!currentUserId.equals(prescription.getPatient().getId())) {
                logAuthorizationDenial("PRESCRIPTION_ACCESS", prescriptionId);
                throw new UnauthorizedException(
                        "Access denied to prescription: " + prescriptionId + 
                        ". Patients can only access their own prescriptions."
                );
            }
            logAuthorizationCheck("PRESCRIPTION_ACCESS", prescriptionId, true);
            return;
        }
        
        // DOCTOR can only access their own prescriptions
        if (currentRole == UserRole.DOCTOR) {
            if (!currentUserId.equals(prescription.getDoctor().getId())) {
                logAuthorizationDenial("PRESCRIPTION_ACCESS", prescriptionId);
                throw new UnauthorizedException(
                        "Access denied to prescription: " + prescriptionId + 
                        ". Doctors can only access their own prescriptions."
                );
            }
            logAuthorizationCheck("PRESCRIPTION_ACCESS", prescriptionId, true);
            return;
        }
        
        // PHARMACIST can access prescriptions in their hospital
        if (currentRole == UserRole.PHARMACIST) {
            Long userHospitalId = securityUtils.getCurrentUserHospitalId();
            if (!userHospitalId.equals(prescription.getHospital().getId())) {
                logAuthorizationDenial("PRESCRIPTION_ACCESS", prescriptionId);
                throw new UnauthorizedException(
                        "Access denied to prescription: " + prescriptionId + 
                        ". Prescription is in hospital: " + prescription.getHospital().getId() +
                        ". User is in hospital: " + userHospitalId
                );
            }
            logAuthorizationCheck("PRESCRIPTION_ACCESS", prescriptionId, true);
            return;
        }
        
        // DIRECTOR can access prescriptions in their hospital
        Long userHospitalId = securityUtils.getCurrentUserHospitalId();
        if (!userHospitalId.equals(prescription.getHospital().getId())) {
            logAuthorizationDenial("PRESCRIPTION_ACCESS", prescriptionId);
            throw new UnauthorizedException(
                    "Access denied to prescription: " + prescriptionId + 
                    ". Prescription is in hospital: " + prescription.getHospital().getId() +
                    ". User is in hospital: " + userHospitalId
            );
        }
        
        logAuthorizationCheck("PRESCRIPTION_ACCESS", prescriptionId, true);
    }
    
    // ==================== Pharmacy Stock Access ====================
    
    /**
     * Verify if current user can access pharmacy stock
     * 
     * @param stockId Pharmacy stock ID to verify access
     * @throws UnauthorizedException if user cannot access stock
     */
    public void verifyPharmacyStockAccess(Long stockId) {
        // ADMIN can access any stock
        if (securityUtils.isAdmin()) {
            logAuthorizationCheck("PHARMACY_STOCK_ACCESS", stockId, true);
            return;
        }
        
        PharmacyStock stock = pharmacyStockRepository.findById(stockId)
                .orElseThrow(() -> new UnauthorizedException("Pharmacy stock not found: " + stockId));
        
        UserRole currentRole = securityUtils.getCurrentUserRole();
        
        // Only PHARMACIST and DIRECTOR can access pharmacy stock
        if (currentRole != UserRole.PHARMACIST && currentRole != UserRole.DIRECTOR) {
            logAuthorizationDenial("PHARMACY_STOCK_ACCESS", stockId);
            throw new UnauthorizedException(
                    "Access denied to pharmacy stock: " + stockId + 
                    ". Only pharmacists and directors can access pharmacy stock."
            );
        }
        
        // Verify user's hospital matches stock's hospital
        Long userHospitalId = securityUtils.getCurrentUserHospitalId();
        if (!userHospitalId.equals(stock.getHospital().getId())) {
            logAuthorizationDenial("PHARMACY_STOCK_ACCESS", stockId);
            throw new UnauthorizedException(
                    "Access denied to pharmacy stock: " + stockId + 
                    ". Stock is in hospital: " + stock.getHospital().getId() +
                    ". User is in hospital: " + userHospitalId
            );
        }
        
        logAuthorizationCheck("PHARMACY_STOCK_ACCESS", stockId, true);
    }
    
    // ==================== Logging ====================
    
    /**
     * Log successful authorization check
     */
    private void logAuthorizationCheck(String action, Object resource, boolean allowed) {
        String userId = securityUtils.getCurrentUserId().toString();
        String role = securityUtils.getCurrentUserRole().toString();
        log.debug("Authorization check: user={}, role={}, action={}, resource={}, allowed={}", 
                userId, role, action, resource, allowed);
    }
    
    /**
     * Log authorization denial
     */
    private void logAuthorizationDenial(String action, Object resource) {
        String userId = securityUtils.getCurrentUserId().toString();
        String role = securityUtils.getCurrentUserRole().toString();
        log.warn("Authorization denied: user={}, role={}, action={}, resource={}", 
                userId, role, action, resource);
    }
}
