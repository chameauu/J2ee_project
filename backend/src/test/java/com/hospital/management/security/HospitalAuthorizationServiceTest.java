package com.hospital.management.security;

import com.hospital.management.entities.Hospital;
import com.hospital.management.entities.HospitalDirector;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HospitalAuthorizationService.
 * Phase 10.4: Hospital-Scoped Authorization Rules
 */
@ExtendWith(MockitoExtension.class)
class HospitalAuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HospitalDirectorRepository hospitalDirectorRepository;

    @InjectMocks
    private HospitalAuthorizationService authorizationService;

    // Test isDirectorOfHospital()

    @Test
    void shouldReturnTrueWhenUserIsDirectorOfHospital() {
        // Given
        Long hospitalId = 1L;
        String userEmail = "director@hospital.com";

        Hospital hospital = new Hospital();
        hospital.setId(hospitalId);

        HospitalDirector director = new HospitalDirector();
        director.setId(10L);
        director.setEmail(userEmail);
        director.setHospital(hospital);

        User user = director; // HospitalDirector extends User

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(hospitalDirectorRepository.findById(user.getId())).thenReturn(Optional.of(director));

        // When
        boolean result = authorizationService.isDirectorOfHospital(hospitalId, userEmail);

        // Then
        assertTrue(result);
        verify(userRepository).findByEmail(userEmail);
        verify(hospitalDirectorRepository).findById(user.getId());
    }

    @Test
    void shouldReturnFalseWhenUserIsDirectorOfDifferentHospital() {
        // Given
        Long requestedHospitalId = 1L;
        Long directorHospitalId = 2L;
        String userEmail = "director@hospital.com";

        Hospital hospital = new Hospital();
        hospital.setId(directorHospitalId);

        HospitalDirector director = new HospitalDirector();
        director.setId(10L);
        director.setEmail(userEmail);
        director.setHospital(hospital);

        User user = director;

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(hospitalDirectorRepository.findById(user.getId())).thenReturn(Optional.of(director));

        // When
        boolean result = authorizationService.isDirectorOfHospital(requestedHospitalId, userEmail);

        // Then
        assertFalse(result);
        verify(userRepository).findByEmail(userEmail);
        verify(hospitalDirectorRepository).findById(user.getId());
    }

    @Test
    void shouldReturnFalseWhenUserIsNotDirector() {
        // Given
        Long hospitalId = 1L;
        String userEmail = "patient@hospital.com";

        Patient patient = new Patient();
        patient.setId(20L);
        patient.setEmail(userEmail);

        User user = patient;

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(hospitalDirectorRepository.findById(user.getId())).thenReturn(Optional.empty());

        // When
        boolean result = authorizationService.isDirectorOfHospital(hospitalId, userEmail);

        // Then
        assertFalse(result);
        verify(userRepository).findByEmail(userEmail);
        verify(hospitalDirectorRepository).findById(user.getId());
    }

    @Test
    void shouldReturnFalseWhenUserNotFound() {
        // Given
        Long hospitalId = 1L;
        String userEmail = "nonexistent@hospital.com";

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // When
        boolean result = authorizationService.isDirectorOfHospital(hospitalId, userEmail);

        // Then
        assertFalse(result);
        verify(userRepository).findByEmail(userEmail);
        verify(hospitalDirectorRepository, never()).findById(any());
    }

    @Test
    void shouldReturnFalseWhenDirectorHasNoHospital() {
        // Given
        Long hospitalId = 1L;
        String userEmail = "director@hospital.com";

        HospitalDirector director = new HospitalDirector();
        director.setId(10L);
        director.setEmail(userEmail);
        director.setHospital(null); // No hospital assigned

        User user = director;

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(hospitalDirectorRepository.findById(user.getId())).thenReturn(Optional.of(director));

        // When
        boolean result = authorizationService.isDirectorOfHospital(hospitalId, userEmail);

        // Then
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenHospitalIdIsNull() {
        // Given
        String userEmail = "director@hospital.com";

        // When
        boolean result = authorizationService.isDirectorOfHospital(null, userEmail);

        // Then
        assertFalse(result);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void shouldReturnFalseWhenUserEmailIsNull() {
        // Given
        Long hospitalId = 1L;

        // When
        boolean result = authorizationService.isDirectorOfHospital(hospitalId, null);

        // Then
        assertFalse(result);
        verify(userRepository, never()).findByEmail(any());
    }

    // Test belongsToHospital()

    @Test
    void shouldReturnTrueWhenUserBelongsToHospital() {
        // Given
        Long hospitalId = 1L;
        String userEmail = "patient@hospital.com";

        Hospital hospital = new Hospital();
        hospital.setId(hospitalId);

        Patient patient = new Patient();
        patient.setId(20L);
        patient.setEmail(userEmail);
        patient.setHospital(hospital);

        User user = patient;

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // When
        boolean result = authorizationService.belongsToHospital(hospitalId, userEmail);

        // Then
        assertTrue(result);
        verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void shouldReturnFalseWhenUserBelongsToDifferentHospital() {
        // Given
        Long requestedHospitalId = 1L;
        Long userHospitalId = 2L;
        String userEmail = "patient@hospital.com";

        Hospital hospital = new Hospital();
        hospital.setId(userHospitalId);

        Patient patient = new Patient();
        patient.setId(20L);
        patient.setEmail(userEmail);
        patient.setHospital(hospital);

        User user = patient;

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // When
        boolean result = authorizationService.belongsToHospital(requestedHospitalId, userEmail);

        // Then
        assertFalse(result);
        verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void shouldReturnFalseWhenUserHasNoHospital() {
        // Given
        Long hospitalId = 1L;
        String userEmail = "patient@hospital.com";

        Patient patient = new Patient();
        patient.setId(20L);
        patient.setEmail(userEmail);
        patient.setHospital(null); // No hospital assigned

        User user = patient;

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // When
        boolean result = authorizationService.belongsToHospital(hospitalId, userEmail);

        // Then
        assertFalse(result);
    }

    // Test canAccessHospital()

    @Test
    void shouldAllowAdminToAccessAnyHospital() {
        // Given
        Long hospitalId = 1L;
        String userEmail = "admin@hospital.com";

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userEmail);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When
        boolean result = authorizationService.canAccessHospital(hospitalId, authentication);

        // Then
        assertTrue(result);
        verify(authentication).getName();
        verify(authentication).getAuthorities();
        verify(userRepository, never()).findByEmail(any()); // Admin check happens first
    }

    @Test
    void shouldAllowUserToAccessTheirOwnHospital() {
        // Given
        Long hospitalId = 1L;
        String userEmail = "director@hospital.com";

        Hospital hospital = new Hospital();
        hospital.setId(hospitalId);

        HospitalDirector director = new HospitalDirector();
        director.setId(10L);
        director.setEmail(userEmail);
        director.setHospital(hospital);

        User user = director;

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userEmail);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) List.of(new SimpleGrantedAuthority("ROLE_DIRECTOR"))
        );

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // When
        boolean result = authorizationService.canAccessHospital(hospitalId, authentication);

        // Then
        assertTrue(result);
        verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void shouldDenyUserAccessToDifferentHospital() {
        // Given
        Long requestedHospitalId = 1L;
        Long userHospitalId = 2L;
        String userEmail = "director@hospital.com";

        Hospital hospital = new Hospital();
        hospital.setId(userHospitalId);

        HospitalDirector director = new HospitalDirector();
        director.setId(10L);
        director.setEmail(userEmail);
        director.setHospital(hospital);

        User user = director;

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userEmail);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) List.of(new SimpleGrantedAuthority("ROLE_DIRECTOR"))
        );

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // When
        boolean result = authorizationService.canAccessHospital(requestedHospitalId, authentication);

        // Then
        assertFalse(result);
        verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void shouldReturnFalseWhenAuthenticationIsNull() {
        // Given
        Long hospitalId = 1L;

        // When
        boolean result = authorizationService.canAccessHospital(hospitalId, null);

        // Then
        assertFalse(result);
    }

    // Test isHospitalDirector()

    @Test
    void shouldReturnTrueWhenUserIsHospitalDirector() {
        // Given
        String userEmail = "director@hospital.com";

        HospitalDirector director = new HospitalDirector();
        director.setId(10L);
        director.setEmail(userEmail);

        User user = director;

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(hospitalDirectorRepository.existsById(user.getId())).thenReturn(true);

        // When
        boolean result = authorizationService.isHospitalDirector(userEmail);

        // Then
        assertTrue(result);
        verify(userRepository).findByEmail(userEmail);
        verify(hospitalDirectorRepository).existsById(user.getId());
    }

    @Test
    void shouldReturnFalseWhenUserIsNotHospitalDirector() {
        // Given
        String userEmail = "patient@hospital.com";

        Patient patient = new Patient();
        patient.setId(20L);
        patient.setEmail(userEmail);

        User user = patient;

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(hospitalDirectorRepository.existsById(user.getId())).thenReturn(false);

        // When
        boolean result = authorizationService.isHospitalDirector(userEmail);

        // Then
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenUserEmailIsNullForDirectorCheck() {
        // When
        boolean result = authorizationService.isHospitalDirector(null);

        // Then
        assertFalse(result);
        verify(userRepository, never()).findByEmail(any());
    }

    // Test getUserHospitalId()

    @Test
    void shouldReturnHospitalIdWhenUserHasHospital() {
        // Given
        Long hospitalId = 1L;
        String userEmail = "patient@hospital.com";

        Hospital hospital = new Hospital();
        hospital.setId(hospitalId);

        Patient patient = new Patient();
        patient.setId(20L);
        patient.setEmail(userEmail);
        patient.setHospital(hospital);

        User user = patient;

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // When
        Optional<Long> result = authorizationService.getUserHospitalId(userEmail);

        // Then
        assertTrue(result.isPresent());
        assertEquals(hospitalId, result.get());
        verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void shouldReturnEmptyWhenUserHasNoHospital() {
        // Given
        String userEmail = "patient@hospital.com";

        Patient patient = new Patient();
        patient.setId(20L);
        patient.setEmail(userEmail);
        patient.setHospital(null);

        User user = patient;

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // When
        Optional<Long> result = authorizationService.getUserHospitalId(userEmail);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnEmptyWhenUserNotFoundForHospitalId() {
        // Given
        String userEmail = "nonexistent@hospital.com";

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // When
        Optional<Long> result = authorizationService.getUserHospitalId(userEmail);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnEmptyWhenUserEmailIsNullForHospitalId() {
        // When
        Optional<Long> result = authorizationService.getUserHospitalId(null);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, never()).findByEmail(any());
    }
}
