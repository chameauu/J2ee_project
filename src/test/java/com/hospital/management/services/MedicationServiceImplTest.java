package com.hospital.management.services;

import com.hospital.management.dto.MedicationDTO;
import com.hospital.management.entities.Medication;
import com.hospital.management.enums.MedicationType;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.MedicationMapper;
import com.hospital.management.repositories.MedicationRepository;
import org.junit.jupiter.api.BeforeEach;
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
class MedicationServiceImplTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private MedicationMapper medicationMapper;

    @InjectMocks
    private MedicationServiceImpl medicationService;

    private Medication medication;
    private MedicationDTO medicationDTO;

    @BeforeEach
    void setUp() {
        medication = new Medication();
        medication.setId(1L);
        medication.setName("Aspirin");
        medication.setGenericName("Acetylsalicylic Acid");
        medication.setManufacturer("PharmaCorp");
        medication.setType(MedicationType.TABLET);
        medication.setStrength("500mg");
        medication.setDescription("Pain reliever");

        medicationDTO = new MedicationDTO();
        medicationDTO.setId(1L);
        medicationDTO.setName("Aspirin");
        medicationDTO.setGenericName("Acetylsalicylic Acid");
        medicationDTO.setManufacturer("PharmaCorp");
        medicationDTO.setType(MedicationType.TABLET);
        medicationDTO.setStrength("500mg");
        medicationDTO.setDescription("Pain reliever");
    }

    @Test
    void shouldCreateMedication() {
        when(medicationMapper.toEntity(any(MedicationDTO.class))).thenReturn(medication);
        when(medicationRepository.save(any(Medication.class))).thenReturn(medication);
        when(medicationMapper.toDTO(any(Medication.class))).thenReturn(medicationDTO);

        MedicationDTO result = medicationService.createMedication(medicationDTO);

        assertNotNull(result);
        assertEquals("Aspirin", result.getName());
        verify(medicationRepository, times(1)).save(any(Medication.class));
    }

    @Test
    void shouldGetMedicationById() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));
        when(medicationMapper.toDTO(any(Medication.class))).thenReturn(medicationDTO);

        MedicationDTO result = medicationService.getMedicationById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Aspirin", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenMedicationNotFound() {
        when(medicationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> medicationService.getMedicationById(999L));
    }

    @Test
    void shouldGetAllMedications() {
        List<Medication> medications = Arrays.asList(medication);
        when(medicationRepository.findAll()).thenReturn(medications);
        when(medicationMapper.toDTO(any(Medication.class))).thenReturn(medicationDTO);

        List<MedicationDTO> result = medicationService.getAllMedications();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldUpdateMedication() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));
        when(medicationRepository.save(any(Medication.class))).thenReturn(medication);
        when(medicationMapper.toDTO(any(Medication.class))).thenReturn(medicationDTO);

        medicationDTO.setName("Updated Aspirin");
        MedicationDTO result = medicationService.updateMedication(1L, medicationDTO);

        assertNotNull(result);
        verify(medicationRepository, times(1)).save(any(Medication.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentMedication() {
        when(medicationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> medicationService.updateMedication(999L, medicationDTO));
    }

    @Test
    void shouldDeleteMedication() {
        when(medicationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(medicationRepository).deleteById(1L);

        medicationService.deleteMedication(1L);

        verify(medicationRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentMedication() {
        when(medicationRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> medicationService.deleteMedication(999L));
    }

    @Test
    void shouldSearchMedications() {
        List<Medication> medications = Arrays.asList(medication);
        when(medicationRepository.searchMedications("Aspirin")).thenReturn(medications);
        when(medicationMapper.toDTO(any(Medication.class))).thenReturn(medicationDTO);

        List<MedicationDTO> result = medicationService.searchMedications("Aspirin");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetMedicationsByType() {
        List<Medication> medications = Arrays.asList(medication);
        when(medicationRepository.findByType(MedicationType.TABLET)).thenReturn(medications);
        when(medicationMapper.toDTO(any(Medication.class))).thenReturn(medicationDTO);

        List<MedicationDTO> result = medicationService.getMedicationsByType(MedicationType.TABLET);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetMedicationsByManufacturer() {
        List<Medication> medications = Arrays.asList(medication);
        when(medicationRepository.findByManufacturer("PharmaCorp")).thenReturn(medications);
        when(medicationMapper.toDTO(any(Medication.class))).thenReturn(medicationDTO);

        List<MedicationDTO> result = medicationService.getMedicationsByManufacturer("PharmaCorp");

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
