package com.hospital.management.services;

import com.hospital.management.dto.PharmacistDTO;
import com.hospital.management.entities.Pharmacist;
import com.hospital.management.exceptions.DuplicateResourceException;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.PharmacistMapper;
import com.hospital.management.repositories.PharmacistRepository;
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
class PharmacistServiceImplTest {

    @Mock
    private PharmacistRepository pharmacistRepository;

    @Mock
    private PharmacistMapper pharmacistMapper;

    @InjectMocks
    private PharmacistServiceImpl pharmacistService;

    @Test
    void shouldCreatePharmacist() {
        // Given
        PharmacistDTO dto = new PharmacistDTO();
        dto.setEmail("pharmacist@example.com");
        dto.setLicenseNumber("PLIC123");

        Pharmacist pharmacist = new Pharmacist();
        Pharmacist savedPharmacist = new Pharmacist();
        savedPharmacist.setId(1L);

        when(pharmacistRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(pharmacistRepository.existsByLicenseNumber(dto.getLicenseNumber())).thenReturn(false);
        when(pharmacistMapper.toEntity(dto)).thenReturn(pharmacist);
        when(pharmacistRepository.save(pharmacist)).thenReturn(savedPharmacist);
        when(pharmacistMapper.toDTO(savedPharmacist)).thenReturn(dto);

        // When
        PharmacistDTO result = pharmacistService.createPharmacist(dto);

        // Then
        assertNotNull(result);
        verify(pharmacistRepository).existsByEmail(dto.getEmail());
        verify(pharmacistRepository).existsByLicenseNumber(dto.getLicenseNumber());
        verify(pharmacistRepository).save(pharmacist);
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        PharmacistDTO dto = new PharmacistDTO();
        dto.setEmail("existing@example.com");
        dto.setLicenseNumber("PLIC123");

        when(pharmacistRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> pharmacistService.createPharmacist(dto));
        verify(pharmacistRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenLicenseNumberExists() {
        // Given
        PharmacistDTO dto = new PharmacistDTO();
        dto.setEmail("pharmacist@example.com");
        dto.setLicenseNumber("EXISTING_LIC");

        when(pharmacistRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(pharmacistRepository.existsByLicenseNumber(dto.getLicenseNumber())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> pharmacistService.createPharmacist(dto));
        verify(pharmacistRepository, never()).save(any());
    }

    @Test
    void shouldGetPharmacistById() {
        // Given
        Long pharmacistId = 1L;
        Pharmacist pharmacist = new Pharmacist();
        pharmacist.setId(pharmacistId);
        PharmacistDTO dto = new PharmacistDTO();

        when(pharmacistRepository.findById(pharmacistId)).thenReturn(Optional.of(pharmacist));
        when(pharmacistMapper.toDTO(pharmacist)).thenReturn(dto);

        // When
        PharmacistDTO result = pharmacistService.getPharmacistById(pharmacistId);

        // Then
        assertNotNull(result);
        verify(pharmacistRepository).findById(pharmacistId);
    }

    @Test
    void shouldThrowExceptionWhenPharmacistNotFound() {
        // Given
        Long pharmacistId = 999L;
        when(pharmacistRepository.findById(pharmacistId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> pharmacistService.getPharmacistById(pharmacistId));
    }

    @Test
    void shouldUpdatePharmacist() {
        // Given
        Long pharmacistId = 1L;
        PharmacistDTO dto = new PharmacistDTO();
        dto.setFirstName("Updated");
        dto.setLastName("Pharmacist");
        dto.setEmail("updated@example.com");
        dto.setPhone("+1234567890");
        dto.setLicenseNumber("PLIC123");
        dto.setQualification("PharmD");

        Pharmacist pharmacist = new Pharmacist();
        pharmacist.setId(pharmacistId);

        when(pharmacistRepository.findById(pharmacistId)).thenReturn(Optional.of(pharmacist));
        when(pharmacistRepository.save(pharmacist)).thenReturn(pharmacist);
        when(pharmacistMapper.toDTO(pharmacist)).thenReturn(dto);

        // When
        PharmacistDTO result = pharmacistService.updatePharmacist(pharmacistId, dto);

        // Then
        assertNotNull(result);
        verify(pharmacistRepository).findById(pharmacistId);
        verify(pharmacistRepository).save(pharmacist);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentPharmacist() {
        // Given
        Long pharmacistId = 999L;
        PharmacistDTO dto = new PharmacistDTO();

        when(pharmacistRepository.findById(pharmacistId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> pharmacistService.updatePharmacist(pharmacistId, dto));
        verify(pharmacistRepository, never()).save(any());
    }

    @Test
    void shouldDeletePharmacist() {
        // Given
        Long pharmacistId = 1L;
        Pharmacist pharmacist = new Pharmacist();
        pharmacist.setId(pharmacistId);

        when(pharmacistRepository.findById(pharmacistId)).thenReturn(Optional.of(pharmacist));

        // When
        pharmacistService.deletePharmacist(pharmacistId);

        // Then
        verify(pharmacistRepository).findById(pharmacistId);
        verify(pharmacistRepository).delete(pharmacist);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentPharmacist() {
        // Given
        Long pharmacistId = 999L;
        when(pharmacistRepository.findById(pharmacistId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> pharmacistService.deletePharmacist(pharmacistId));
        verify(pharmacistRepository, never()).delete(any());
    }

    @Test
    void shouldGetAllPharmacists() {
        // Given
        Pharmacist pharmacist1 = new Pharmacist();
        Pharmacist pharmacist2 = new Pharmacist();
        List<Pharmacist> pharmacists = Arrays.asList(pharmacist1, pharmacist2);

        PharmacistDTO dto1 = new PharmacistDTO();
        PharmacistDTO dto2 = new PharmacistDTO();

        when(pharmacistRepository.findAll()).thenReturn(pharmacists);
        when(pharmacistMapper.toDTO(pharmacist1)).thenReturn(dto1);
        when(pharmacistMapper.toDTO(pharmacist2)).thenReturn(dto2);

        // When
        List<PharmacistDTO> result = pharmacistService.getAllPharmacists();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(pharmacistRepository).findAll();
    }
}
