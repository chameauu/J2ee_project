package com.hospital.management.services;

import com.hospital.management.dto.DoctorDTO;
import com.hospital.management.entities.Doctor;
import com.hospital.management.exceptions.DuplicateResourceException;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.DoctorMapper;
import com.hospital.management.repositories.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    @Test
    void shouldCreateDoctor() {
        // Given
        DoctorDTO dto = new DoctorDTO();
        dto.setEmail("doctor@example.com");
        dto.setLicenseNumber("LIC123");

        Doctor doctor = new Doctor();
        Doctor savedDoctor = new Doctor();
        savedDoctor.setId(1L);

        when(doctorRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(doctorRepository.existsByLicenseNumber(dto.getLicenseNumber())).thenReturn(false);
        when(doctorMapper.toEntity(dto)).thenReturn(doctor);
        when(doctorRepository.save(doctor)).thenReturn(savedDoctor);
        when(doctorMapper.toDTO(savedDoctor)).thenReturn(dto);

        // When
        DoctorDTO result = doctorService.createDoctor(dto);

        // Then
        assertNotNull(result);
        verify(doctorRepository).existsByEmail(dto.getEmail());
        verify(doctorRepository).existsByLicenseNumber(dto.getLicenseNumber());
        verify(doctorRepository).save(doctor);
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        DoctorDTO dto = new DoctorDTO();
        dto.setEmail("existing@example.com");
        dto.setLicenseNumber("LIC123");

        when(doctorRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> doctorService.createDoctor(dto));
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenLicenseNumberExists() {
        // Given
        DoctorDTO dto = new DoctorDTO();
        dto.setEmail("doctor@example.com");
        dto.setLicenseNumber("EXISTING_LIC");

        when(doctorRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(doctorRepository.existsByLicenseNumber(dto.getLicenseNumber())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> doctorService.createDoctor(dto));
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void shouldGetDoctorById() {
        // Given
        Long doctorId = 1L;
        Doctor doctor = new Doctor();
        doctor.setId(doctorId);
        DoctorDTO dto = new DoctorDTO();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toDTO(doctor)).thenReturn(dto);

        // When
        DoctorDTO result = doctorService.getDoctorById(doctorId);

        // Then
        assertNotNull(result);
        verify(doctorRepository).findById(doctorId);
    }

    @Test
    void shouldThrowExceptionWhenDoctorNotFound() {
        // Given
        Long doctorId = 999L;
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> doctorService.getDoctorById(doctorId));
    }

    @Test
    void shouldUpdateDoctor() {
        // Given
        Long doctorId = 1L;
        DoctorDTO dto = new DoctorDTO();
        dto.setFirstName("Updated");
        dto.setLastName("Doctor");
        dto.setEmail("updated@example.com");
        dto.setPhone("+1234567890");
        dto.setSpecialization("Cardiology");
        dto.setLicenseNumber("LIC123");
        dto.setYearsOfExperience(10);
        dto.setQualification("MD");

        Doctor doctor = new Doctor();
        doctor.setId(doctorId);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctorMapper.toDTO(doctor)).thenReturn(dto);

        // When
        DoctorDTO result = doctorService.updateDoctor(doctorId, dto);

        // Then
        assertNotNull(result);
        verify(doctorRepository).findById(doctorId);
        verify(doctorRepository).save(doctor);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentDoctor() {
        // Given
        Long doctorId = 999L;
        DoctorDTO dto = new DoctorDTO();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> doctorService.updateDoctor(doctorId, dto));
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void shouldDeleteDoctor() {
        // Given
        Long doctorId = 1L;
        Doctor doctor = new Doctor();
        doctor.setId(doctorId);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        // When
        doctorService.deleteDoctor(doctorId);

        // Then
        verify(doctorRepository).findById(doctorId);
        verify(doctorRepository).delete(doctor);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentDoctor() {
        // Given
        Long doctorId = 999L;
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> doctorService.deleteDoctor(doctorId));
        verify(doctorRepository, never()).delete(any());
    }

    @Test
    void shouldGetAllDoctors() {
        // Given
        Doctor doctor1 = new Doctor();
        Doctor doctor2 = new Doctor();
        List<Doctor> doctors = Arrays.asList(doctor1, doctor2);

        DoctorDTO dto1 = new DoctorDTO();
        DoctorDTO dto2 = new DoctorDTO();

        when(doctorRepository.findAll()).thenReturn(doctors);
        when(doctorMapper.toDTO(doctor1)).thenReturn(dto1);
        when(doctorMapper.toDTO(doctor2)).thenReturn(dto2);

        // When
        List<DoctorDTO> result = doctorService.getAllDoctors();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(doctorRepository).findAll();
    }

    @Test
    void shouldGetDoctorsBySpecialization() {
        // Given
        String specialization = "Cardiology";
        Doctor doctor1 = new Doctor();
        Doctor doctor2 = new Doctor();
        List<Doctor> doctors = Arrays.asList(doctor1, doctor2);

        DoctorDTO dto1 = new DoctorDTO();
        DoctorDTO dto2 = new DoctorDTO();

        when(doctorRepository.findBySpecialization(specialization)).thenReturn(doctors);
        when(doctorMapper.toDTO(doctor1)).thenReturn(dto1);
        when(doctorMapper.toDTO(doctor2)).thenReturn(dto2);

        // When
        List<DoctorDTO> result = doctorService.getDoctorsBySpecialization(specialization);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(doctorRepository).findBySpecialization(specialization);
    }
}
