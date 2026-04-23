package com.hospital.management.services;

import com.hospital.management.dto.PatientDTO;
import com.hospital.management.entities.Patient;
import com.hospital.management.enums.Gender;
import com.hospital.management.exceptions.DuplicateResourceException;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.PatientMapper;
import com.hospital.management.repositories.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientServiceImpl patientService;

    @Test
    void shouldCreatePatient() {
        // Given
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setPhone("+1234567890");
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        dto.setGender(Gender.MALE);

        Patient patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setEmail("john@example.com");

        Patient savedPatient = new Patient();
        savedPatient.setId(1L);
        savedPatient.setFirstName("John");
        savedPatient.setLastName("Doe");
        savedPatient.setEmail("john@example.com");

        PatientDTO savedDTO = new PatientDTO();
        savedDTO.setId(1L);
        savedDTO.setFirstName("John");
        savedDTO.setLastName("Doe");
        savedDTO.setEmail("john@example.com");

        when(patientRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(patientMapper.toEntity(dto)).thenReturn(patient);
        when(patientRepository.save(patient)).thenReturn(savedPatient);
        when(patientMapper.toDTO(savedPatient)).thenReturn(savedDTO);

        // When
        PatientDTO result = patientService.createPatient(dto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        verify(patientRepository).save(patient);
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        PatientDTO dto = new PatientDTO();
        dto.setEmail("existing@example.com");

        when(patientRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class,
                () -> patientService.createPatient(dto));
        verify(patientRepository, never()).save(any());
    }

    @Test
    void shouldGetPatientById() {
        // Given
        Long patientId = 1L;
        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setFirstName("John");
        patient.setLastName("Doe");

        PatientDTO dto = new PatientDTO();
        dto.setId(patientId);
        dto.setFirstName("John");
        dto.setLastName("Doe");

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientMapper.toDTO(patient)).thenReturn(dto);

        // When
        PatientDTO result = patientService.getPatientById(patientId);

        // Then
        assertNotNull(result);
        assertEquals(patientId, result.getId());
        assertEquals("John", result.getFirstName());
    }

    @Test
    void shouldThrowExceptionWhenPatientNotFound() {
        // Given
        Long patientId = 999L;
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> patientService.getPatientById(patientId));
    }
}
