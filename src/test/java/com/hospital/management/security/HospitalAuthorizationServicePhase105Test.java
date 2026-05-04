package com.hospital.management.security;

import com.hospital.management.entities.Doctor;
import com.hospital.management.entities.Hospital;
import com.hospital.management.entities.Patient;
import com.hospital.management.entities.User;
import com.hospital.management.repositories.HospitalDirectorRepository;
import com.hospital.management.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HospitalAuthorizationService - Phase 10.5 additions.
 * Tests the canAccessUserData() method for medical entity authorization.
 */
@ExtendWith(MockitoExtension.class)
class HospitalAuthorizationServicePhase105Test {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HospitalDirectorRepository hospitalDirectorRepository;

    @InjectMocks
    private HospitalAuthorizationService authorizationService;

    // Test canAccessUserData()

    @Test
    void shouldAllowAdminToAccessAnyUserData() {
        // Given
        Long targetUserId = 10L;
        String adminEmail = "admin@hospital.com";

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(adminEmail);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When
        boolean result = authorizationService.canAccessUserData(targetUserId, authentication);

        // Then
        assertTrue(result);
        verify(authentication).getName();
        verify(authentication).getAuthorities();
        verify(userRepository, never()).findById(any()); // Admin check happens first
    }

    @Test
    void shouldAllowUserToAccessSameHospitalUserData() {
        // Given
        Long targetUserId = 10L;
        Long hospitalId = 1L;
        String userEmail = "doctor@hospital.com";

        Hospital hospital = new Hospital();
        hospital.setId(hospitalId);

        // Target user (patient in same hospital)
        Patient targetUser = new Patient();
        targetUser.setId(targetUserId);
        targetUser.setEmail("patient@hospital.com");
        targetUser.setHospital(hospital);

        // Authenticated user (doctor in same hospital)
        Doctor authUser = new Doctor();
        authUser.setId(20L);
        authUser.setEmail(userEmail);
        authUser.setHospital(hospital);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userEmail);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(authUser));

        // When
        boolean result = authorizationService.canAccessUserData(targetUserId, authentication);

        // Then
        assertTrue(result);
        verify(userRepository).findById(targetUserId);
        verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void shouldDenyUserAccessToDifferentHospitalUserData() {
        // Given
        Long targetUserId = 10L;
        Long hospital1Id = 1L;
        Long hospital2Id = 2L;
        String userEmail = "doctor@hospital.com";

        Hospital hospital1 = new Hospital();
        hospital1.setId(hospital1Id);

        Hospital hospital2 = new Hospital();
        hospital2.setId(hospital2Id);

        // Target user (patient in hospital 1)
        Patient targetUser = new Patient();
        targetUser.setId(targetUserId);
        targetUser.setEmail("patient@hospital.com");
        targetUser.setHospital(hospital1);

        // Authenticated user (doctor in hospital 2)
        Doctor authUser = new Doctor();
        authUser.setId(20L);
        authUser.setEmail(userEmail);
        authUser.setHospital(hospital2);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userEmail);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(authUser));

        // When
        boolean result = authorizationService.canAccessUserData(targetUserId, authentication);

        // Then
        assertFalse(result);
        verify(userRepository).findById(targetUserId);
        verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void shouldReturnFalseWhenTargetUserNotFound() {
        // Given
        Long targetUserId = 999L;
        String userEmail = "doctor@hospital.com";

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userEmail);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );

        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        // When
        boolean result = authorizationService.canAccessUserData(targetUserId, authentication);

        // Then
        assertFalse(result);
        verify(userRepository).findById(targetUserId);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void shouldReturnFalseWhenTargetUserHasNoHospital() {
        // Given
        Long targetUserId = 10L;
        String userEmail = "doctor@hospital.com";

        // Target user with no hospital
        Patient targetUser = new Patient();
        targetUser.setId(targetUserId);
        targetUser.setEmail("patient@hospital.com");
        targetUser.setHospital(null);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userEmail);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));

        // When
        boolean result = authorizationService.canAccessUserData(targetUserId, authentication);

        // Then
        assertFalse(result);
        verify(userRepository).findById(targetUserId);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void shouldReturnFalseWhenUserIdIsNull() {
        // Given
        String userEmail = "doctor@hospital.com";

        Authentication authentication = mock(Authentication.class);

        // When
        boolean result = authorizationService.canAccessUserData(null, authentication);

        // Then
        assertFalse(result);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void shouldReturnFalseWhenAuthenticationIsNull() {
        // Given
        Long targetUserId = 10L;

        // When
        boolean result = authorizationService.canAccessUserData(targetUserId, null);

        // Then
        assertFalse(result);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void shouldHandleExceptionGracefully() {
        // Given
        Long targetUserId = 10L;
        String userEmail = "doctor@hospital.com";

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userEmail);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );

        when(userRepository.findById(targetUserId)).thenThrow(new RuntimeException("Database error"));

        // When
        boolean result = authorizationService.canAccessUserData(targetUserId, authentication);

        // Then
        assertFalse(result);
        verify(userRepository).findById(targetUserId);
    }
}
