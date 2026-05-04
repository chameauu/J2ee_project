package com.hospital.management.security;

import com.hospital.management.enums.UserRole;
import com.hospital.management.entities.User;
import com.hospital.management.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Security Utilities
 * 
 * Provides convenient methods to access current user information from Spring Security context.
 * Used throughout the application for authorization checks and user context retrieval.
 * 
 * Phase 10.7: Repository Authorization
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityUtils {
    
    private final UserRepository userRepository;
    
    /**
     * Get the current authenticated user's email
     * 
     * @return User email or null if not authenticated
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }
    
    /**
     * Get the current authenticated user's ID
     * 
     * @return User ID or null if not authenticated
     */
    public Long getCurrentUserId() {
        String email = getCurrentUserEmail();
        if (email == null) {
            return null;
        }
        
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("User not found with email: {}", email);
                return null;
            }
            return userOpt.get().getId();
        } catch (Exception e) {
            log.error("Error getting current user ID: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Get the current authenticated user's role
     * 
     * @return UserRole or null if not authenticated
     */
    public UserRole getCurrentUserRole() {
        String email = getCurrentUserEmail();
        if (email == null) {
            return null;
        }
        
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("User not found with email: {}", email);
                return null;
            }
            return userOpt.get().getRole();
        } catch (Exception e) {
            log.error("Error getting current user role: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Get the current authenticated user's hospital ID
     * 
     * @return Hospital ID or null if user has no hospital
     */
    public Long getCurrentUserHospitalId() {
        String email = getCurrentUserEmail();
        if (email == null) {
            return null;
        }
        
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("User not found with email: {}", email);
                return null;
            }
            
            User user = userOpt.get();
            if (user.getHospital() == null) {
                log.debug("User {} has no hospital assigned", email);
                return null;
            }
            
            return user.getHospital().getId();
        } catch (Exception e) {
            log.error("Error getting current user hospital ID: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if current user is ADMIN
     * 
     * @return true if user is ADMIN, false otherwise
     */
    public boolean isAdmin() {
        UserRole role = getCurrentUserRole();
        return role == UserRole.ADMIN;
    }
    
    /**
     * Check if current user is DIRECTOR
     * 
     * @return true if user is DIRECTOR, false otherwise
     */
    public boolean isDirector() {
        UserRole role = getCurrentUserRole();
        return role == UserRole.DIRECTOR;
    }
    
    /**
     * Check if current user is DOCTOR
     * 
     * @return true if user is DOCTOR, false otherwise
     */
    public boolean isDoctor() {
        UserRole role = getCurrentUserRole();
        return role == UserRole.DOCTOR;
    }
    
    /**
     * Check if current user is PHARMACIST
     * 
     * @return true if user is PHARMACIST, false otherwise
     */
    public boolean isPharmacist() {
        UserRole role = getCurrentUserRole();
        return role == UserRole.PHARMACIST;
    }
    
    /**
     * Check if current user is PATIENT
     * 
     * @return true if user is PATIENT, false otherwise
     */
    public boolean isPatient() {
        UserRole role = getCurrentUserRole();
        return role == UserRole.PATIENT;
    }
    
    /**
     * Check if current user is authenticated
     * 
     * @return true if user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
    
    /**
     * Get the current authenticated user
     * 
     * @return User object or empty Optional if not authenticated
     */
    public Optional<User> getCurrentUser() {
        String email = getCurrentUserEmail();
        if (email == null) {
            return Optional.empty();
        }
        
        try {
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
