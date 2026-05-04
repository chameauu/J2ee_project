package com.hospital.management.security;

import com.hospital.management.entities.Appointment;
import com.hospital.management.entities.HospitalDirector;
import com.hospital.management.entities.MedicalRecord;
import com.hospital.management.entities.Prescription;
import com.hospital.management.entities.User;
import com.hospital.management.repositories.AppointmentRepository;
import com.hospital.management.repositories.HospitalDirectorRepository;
import com.hospital.management.repositories.MedicalRecordRepository;
import com.hospital.management.repositories.PrescriptionRepository;
import com.hospital.management.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for hospital-based authorization checks.
 * Used in @PreAuthorize SpEL expressions to verify hospital ownership.
 * 
 * Phase 10.4: Hospital-Scoped Authorization Rules
 */
@Service("hospitalAuthorizationService")
@RequiredArgsConstructor
@Slf4j
public class HospitalAuthorizationService {

    private final UserRepository userRepository;
    private final HospitalDirectorRepository hospitalDirectorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    /**
     * Check if the authenticated user is a director of the specified hospital.
     * 
     * @param hospitalId The hospital ID to check
     * @param userEmail The email of the authenticated user
     * @return true if user is director of the hospital, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isDirectorOfHospital(Long hospitalId, String userEmail) {
        if (hospitalId == null || userEmail == null) {
            log.warn("Hospital ID or user email is null");
            return false;
        }

        try {
            // Find the user by email
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                log.warn("User not found with email: {}", userEmail);
                return false;
            }

            User user = userOpt.get();

            // Check if user is a hospital director
            Optional<HospitalDirector> directorOpt = hospitalDirectorRepository.findById(user.getId());
            if (directorOpt.isEmpty()) {
                log.debug("User {} is not a hospital director", userEmail);
                return false;
            }

            HospitalDirector director = directorOpt.get();

            // Check if director's hospital matches the requested hospital
            if (director.getHospital() == null) {
                log.warn("Director {} has no hospital assigned", userEmail);
                return false;
            }

            boolean isAuthorized = director.getHospital().getId().equals(hospitalId);
            
            if (!isAuthorized) {
                log.warn("Director {} attempted to access hospital {} but belongs to hospital {}", 
                    userEmail, hospitalId, director.getHospital().getId());
            }

            return isAuthorized;

        } catch (Exception e) {
            log.error("Error checking director authorization for hospital {}: {}", hospitalId, e.getMessage());
            return false;
        }
    }

    /**
     * Check if the authenticated user belongs to the specified hospital.
     * Works for any user type (Patient, Doctor, Pharmacist, Administrator, Director).
     * 
     * @param hospitalId The hospital ID to check
     * @param userEmail The email of the authenticated user
     * @return true if user belongs to the hospital, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean belongsToHospital(Long hospitalId, String userEmail) {
        if (hospitalId == null || userEmail == null) {
            log.warn("Hospital ID or user email is null");
            return false;
        }

        try {
            // Find the user by email
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                log.warn("User not found with email: {}", userEmail);
                return false;
            }

            User user = userOpt.get();

            // Check if user's hospital matches the requested hospital
            if (user.getHospital() == null) {
                log.debug("User {} has no hospital assigned", userEmail);
                return false;
            }

            boolean belongs = user.getHospital().getId().equals(hospitalId);
            
            if (!belongs) {
                log.warn("User {} attempted to access hospital {} but belongs to hospital {}", 
                    userEmail, hospitalId, user.getHospital().getId());
            }

            return belongs;

        } catch (Exception e) {
            log.error("Error checking user hospital membership for hospital {}: {}", hospitalId, e.getMessage());
            return false;
        }
    }

    /**
     * Check if the authenticated user can access the specified hospital's data.
     * ADMIN can access any hospital, others must belong to the hospital.
     * 
     * @param hospitalId The hospital ID to check
     * @param authentication The Spring Security authentication object
     * @return true if user can access the hospital, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean canAccessHospital(Long hospitalId, Authentication authentication) {
        if (hospitalId == null || authentication == null) {
            log.warn("Hospital ID or authentication is null");
            return false;
        }

        String userEmail = authentication.getName();

        // ADMIN can access any hospital
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            log.debug("Admin user {} accessing hospital {}", userEmail, hospitalId);
            return true;
        }

        // Other users must belong to the hospital
        return belongsToHospital(hospitalId, userEmail);
    }

    /**
     * Check if the authenticated user is a director of any hospital.
     * 
     * @param userEmail The email of the authenticated user
     * @return true if user is a hospital director, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isHospitalDirector(String userEmail) {
        if (userEmail == null) {
            return false;
        }

        try {
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                return false;
            }

            return hospitalDirectorRepository.existsById(userOpt.get().getId());

        } catch (Exception e) {
            log.error("Error checking if user is director: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get the hospital ID of the authenticated user.
     * 
     * @param userEmail The email of the authenticated user
     * @return Optional containing the hospital ID, or empty if user has no hospital
     */
    @Transactional(readOnly = true)
    public Optional<Long> getUserHospitalId(String userEmail) {
        if (userEmail == null) {
            return Optional.empty();
        }

        try {
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                return Optional.empty();
            }

            User user = userOpt.get();
            if (user.getHospital() == null) {
                return Optional.empty();
            }

            return Optional.of(user.getHospital().getId());

        } catch (Exception e) {
            log.error("Error getting user hospital ID: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Check if authenticated user can access data belonging to a specific user.
     * ADMIN can access any user's data.
     * DOCTOR can only access their own data or their patients' data.
     * PATIENT can only access their own data.
     * DIRECTOR can access data from users in their hospital.
     * 
     * Phase 10.5: Medical Entity Authorization
     * Phase 10.8: Enhanced with ownership checks
     * 
     * @param userId The ID of the user whose data is being accessed
     * @param authentication The Spring Security authentication object
     * @return true if user can access the data, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean canAccessUserData(Long userId, Authentication authentication) {
        if (userId == null || authentication == null) {
            log.warn("User ID or authentication is null");
            return false;
        }

        String userEmail = authentication.getName();

        // ADMIN can access any user's data
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            log.debug("Admin user {} accessing user {} data", userEmail, userId);
            return true;
        }

        try {
            // Get the authenticated user
            Optional<User> authUserOpt = userRepository.findByEmail(userEmail);
            if (authUserOpt.isEmpty()) {
                log.warn("Authenticated user not found with email: {}", userEmail);
                return false;
            }

            User authUser = authUserOpt.get();

            // Check if user is accessing their own data
            if (authUser.getId().equals(userId)) {
                log.debug("User {} accessing their own data", userEmail);
                return true;
            }

            // Get the target user's hospital
            Optional<User> targetUserOpt = userRepository.findById(userId);
            if (targetUserOpt.isEmpty()) {
                log.warn("Target user not found with id: {}", userId);
                return false;
            }

            User targetUser = targetUserOpt.get();
            if (targetUser.getHospital() == null) {
                log.debug("Target user {} has no hospital assigned", userId);
                return false;
            }

            Long targetHospitalId = targetUser.getHospital().getId();

            // DIRECTOR can access data from users in their hospital
            boolean isDirector = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_DIRECTOR"));
            
            if (isDirector) {
                boolean canAccess = belongsToHospital(targetHospitalId, userEmail);
                if (!canAccess) {
                    log.warn("Director {} attempted to access user {} from different hospital", userEmail, userId);
                }
                return canAccess;
            }

            // DOCTOR and PATIENT can only access their own data (already checked above)
            log.warn("User {} attempted to access user {} data without permission", userEmail, userId);
            return false;

        } catch (Exception e) {
            log.error("Error checking user data access for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Check if authenticated user is the owner of the specified user ID.
     * Used for self-access checks (e.g., patient accessing their own records).
     * 
     * Phase 10.8: Ownership validation
     * 
     * @param userId The ID to check ownership for
     * @param authentication The Spring Security authentication object
     * @return true if authenticated user owns the ID, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isOwner(Long userId, Authentication authentication) {
        if (userId == null || authentication == null) {
            return false;
        }

        String userEmail = authentication.getName();

        try {
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                return false;
            }

            return userOpt.get().getId().equals(userId);

        } catch (Exception e) {
            log.error("Error checking ownership: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if authenticated user can access a specific appointment.
     * Patients can only access their own appointments.
     * 
     * @param appointmentId The appointment ID to check
     * @param authentication The Spring Security authentication object
     * @return true if user can access the appointment, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean canAccessAppointment(Long appointmentId, Authentication authentication) {
        if (appointmentId == null || authentication == null) {
            return false;
        }

        String userEmail = authentication.getName();

        try {
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                log.warn("User not found with email: {}", userEmail);
                return false;
            }

            User user = userOpt.get();

            // Get the appointment
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (appointmentOpt.isEmpty()) {
                log.warn("Appointment not found with id: {}", appointmentId);
                return false;
            }

            Appointment appointment = appointmentOpt.get();

            // Check if the appointment belongs to this patient
            if (appointment.getPatient() != null && 
                appointment.getPatient().getId().equals(user.getId())) {
                return true;
            }

            log.warn("User {} attempted to access appointment {} without permission", userEmail, appointmentId);
            return false;

        } catch (Exception e) {
            log.error("Error checking appointment access: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if authenticated user can access a specific prescription.
     * Patients can only access their own prescriptions.
     * 
     * @param prescriptionId The prescription ID to check
     * @param authentication The Spring Security authentication object
     * @return true if user can access the prescription, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean canAccessPrescription(Long prescriptionId, Authentication authentication) {
        if (prescriptionId == null || authentication == null) {
            return false;
        }

        String userEmail = authentication.getName();

        try {
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                log.warn("User not found with email: {}", userEmail);
                return false;
            }

            User user = userOpt.get();

            // Get the prescription
            Optional<Prescription> prescriptionOpt = prescriptionRepository.findById(prescriptionId);
            if (prescriptionOpt.isEmpty()) {
                log.warn("Prescription not found with id: {}", prescriptionId);
                return false;
            }

            Prescription prescription = prescriptionOpt.get();

            // Check if the prescription belongs to this patient
            if (prescription.getPatient() != null && 
                prescription.getPatient().getId().equals(user.getId())) {
                return true;
            }

            log.warn("User {} attempted to access prescription {} without permission", userEmail, prescriptionId);
            return false;

        } catch (Exception e) {
            log.error("Error checking prescription access: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if a doctor can access a patient's data.
     * Doctors can access patients if they have medical records or appointments with them.
     * 
     * Phase 10.11: Doctor-Patient relationship authorization
     * 
     * @param patientId The patient ID to check
     * @param authentication The Spring Security authentication object
     * @return true if doctor can access the patient, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean doctorCanAccessPatient(Long patientId, Authentication authentication) {
        if (patientId == null || authentication == null) {
            return false;
        }

        String userEmail = authentication.getName();

        try {
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                log.warn("User not found with email: {}", userEmail);
                return false;
            }

            User user = userOpt.get();

            // Check if doctor has any medical records with this patient
            boolean hasMedicalRecords = medicalRecordRepository.existsByDoctorIdAndPatientId(user.getId(), patientId);
            if (hasMedicalRecords) {
                log.debug("Doctor {} has medical records with patient {}", userEmail, patientId);
                return true;
            }

            // Check if doctor has any appointments with this patient
            boolean hasAppointments = appointmentRepository.existsByDoctorIdAndPatientId(user.getId(), patientId);
            if (hasAppointments) {
                log.debug("Doctor {} has appointments with patient {}", userEmail, patientId);
                return true;
            }

            // Check if they're in the same hospital (doctors can see patients in their hospital)
            Optional<User> patientOpt = userRepository.findById(patientId);
            if (patientOpt.isPresent()) {
                User patient = patientOpt.get();
                if (user.getHospital() != null && patient.getHospital() != null &&
                    user.getHospital().getId().equals(patient.getHospital().getId())) {
                    log.debug("Doctor {} and patient {} are in the same hospital", userEmail, patientId);
                    return true;
                }
            }

            log.warn("Doctor {} attempted to access patient {} without relationship", userEmail, patientId);
            return false;

        } catch (Exception e) {
            log.error("Error checking doctor-patient access: {}", e.getMessage());
            return false;
        }
    }
}
