package com.hospital.management.services;

import com.hospital.management.dto.PatientDTO;
import com.hospital.management.entities.Patient;
import com.hospital.management.enums.Gender;
import com.hospital.management.exceptions.DuplicateResourceException;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.PatientMapper;
import com.hospital.management.repositories.HospitalRepository;
import com.hospital.management.repositories.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
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

    @Mock
    private HospitalRepository hospitalRepository;

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

    @Test
    void shouldUpdatePatient() {
        // Given
        Long patientId = 1L;
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("UpdatedName");
        dto.setLastName("UpdatedLastName");
        dto.setEmail("updated@example.com");
        dto.setPhone("+9999999999");

        Patient existingPatient = new Patient();
        existingPatient.setId(patientId);
        existingPatient.setFirstName("OldName");
        existingPatient.setLastName("OldLastName");
        existingPatient.setEmail("old@example.com");

        Patient updatedPatient = new Patient();
        updatedPatient.setId(patientId);
        updatedPatient.setFirstName("UpdatedName");
        updatedPatient.setLastName("UpdatedLastName");
        updatedPatient.setEmail("updated@example.com");

        PatientDTO updatedDTO = new PatientDTO();
        updatedDTO.setId(patientId);
        updatedDTO.setFirstName("UpdatedName");
        updatedDTO.setLastName("UpdatedLastName");

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(updatedPatient);
        when(patientMapper.toDTO(updatedPatient)).thenReturn(updatedDTO);

        // When
        PatientDTO result = patientService.updatePatient(patientId, dto);

        // Then
        assertNotNull(result);
        assertEquals("UpdatedName", result.getFirstName());
        assertEquals("UpdatedLastName", result.getLastName());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentPatient() {
        // Given
        Long patientId = 999L;
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("John");

        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> patientService.updatePatient(patientId, dto));
        verify(patientRepository, never()).save(any());
    }

    @Test
    void shouldDeletePatient() {
        // Given
        Long patientId = 1L;
        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setFirstName("John");

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        // When
        patientService.deletePatient(patientId);

        // Then
        verify(patientRepository).delete(patient);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentPatient() {
        // Given
        Long patientId = 999L;
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> patientService.deletePatient(patientId));
        verify(patientRepository, never()).delete(any());
    }

    @Test
    void shouldGetAllPatients() {
        // Given
        Patient patient1 = new Patient();
        patient1.setId(1L);
        patient1.setFirstName("John");

        Patient patient2 = new Patient();
        patient2.setId(2L);
        patient2.setFirstName("Jane");

        List<Patient> patients = Arrays.asList(patient1, patient2);

        PatientDTO dto1 = new PatientDTO();
        dto1.setId(1L);
        dto1.setFirstName("John");

        PatientDTO dto2 = new PatientDTO();
        dto2.setId(2L);
        dto2.setFirstName("Jane");

        when(patientRepository.findAll()).thenReturn(patients);
        when(patientMapper.toDTO(patient1)).thenReturn(dto1);
        when(patientMapper.toDTO(patient2)).thenReturn(dto2);

        // When
        List<PatientDTO> result = patientService.getAllPatients();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());
    }

    // Phase 10.3: Hospital-scoped query tests
    @Test
    void shouldGetPatientsByHospital() {
        // Given
        Long hospitalId = 1L;
        Patient patient1 = new Patient();
        patient1.setId(1L);
        patient1.setFirstName("John");

        Patient patient2 = new Patient();
        patient2.setId(2L);
        patient2.setFirstName("Jane");

        List<Patient> patients = Arrays.asList(patient1, patient2);

        PatientDTO dto1 = new PatientDTO();
        dto1.setId(1L);
        dto1.setFirstName("John");
        dto1.setHospitalId(hospitalId);

        PatientDTO dto2 = new PatientDTO();
        dto2.setId(2L);
        dto2.setFirstName("Jane");
        dto2.setHospitalId(hospitalId);

        when(hospitalRepository.existsById(hospitalId)).thenReturn(true);
        when(patientRepository.findByHospitalId(hospitalId)).thenReturn(patients);
        when(patientMapper.toDTO(patient1)).thenReturn(dto1);
        when(patientMapper.toDTO(patient2)).thenReturn(dto2);

        // When
        List<PatientDTO> result = patientService.getPatientsByHospital(hospitalId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());
        assertEquals(hospitalId, result.get(0).getHospitalId());
        verify(hospitalRepository).existsById(hospitalId);
        verify(patientRepository).findByHospitalId(hospitalId);
    }

    @Test
    void shouldThrowExceptionWhenHospitalNotFoundForGetPatients() {
        // Given
        Long hospitalId = 999L;
        when(hospitalRepository.existsById(hospitalId)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> patientService.getPatientsByHospital(hospitalId));
        verify(patientRepository, never()).findByHospitalId(any());
    }

    @Test
    void shouldCountPatientsByHospital() {
        // Given
        Long hospitalId = 1L;
        Long expectedCount = 150L;

        when(hospitalRepository.existsById(hospitalId)).thenReturn(true);
        when(patientRepository.countByHospitalId(hospitalId)).thenReturn(expectedCount);

        // When
        Long result = patientService.countPatientsByHospital(hospitalId);

        // Then
        assertNotNull(result);
        assertEquals(expectedCount, result);
        verify(hospitalRepository).existsById(hospitalId);
        verify(patientRepository).countByHospitalId(hospitalId);
    }

    @Test
    void shouldThrowExceptionWhenHospitalNotFoundForCountPatients() {
        // Given
        Long hospitalId = 999L;
        when(hospitalRepository.existsById(hospitalId)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> patientService.countPatientsByHospital(hospitalId));
        verify(patientRepository, never()).countByHospitalId(any());
    }
}
