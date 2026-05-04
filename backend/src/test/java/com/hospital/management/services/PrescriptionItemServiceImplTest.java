package com.hospital.management.services;

import com.hospital.management.dto.PrescriptionItemDTO;
import com.hospital.management.entities.*;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.PrescriptionItemMapper;
import com.hospital.management.repositories.*;
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
class PrescriptionItemServiceImplTest {

    @Mock
    private PrescriptionItemRepository prescriptionItemRepository;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private PharmacistRepository pharmacistRepository;

    @Mock
    private PrescriptionItemMapper prescriptionItemMapper;

    @InjectMocks
    private PrescriptionItemServiceImpl prescriptionItemService;

    private Prescription prescription;
    private Medication medication;
    private Pharmacist pharmacist;
    private PrescriptionItem prescriptionItem;
    private PrescriptionItemDTO prescriptionItemDTO;

    @BeforeEach
    void setUp() {
        prescription = new Prescription();
        prescription.setId(1L);

        medication = new Medication();
        medication.setId(1L);
        medication.setName("Aspirin");

        pharmacist = new Pharmacist();
        pharmacist.setId(1L);
        pharmacist.setFirstName("John");

        prescriptionItem = new PrescriptionItem();
        prescriptionItem.setId(1L);
        prescriptionItem.setPrescription(prescription);
        prescriptionItem.setMedication(medication);
        prescriptionItem.setDosage("500mg");
        prescriptionItem.setFrequency("Twice daily");
        prescriptionItem.setDurationDays(7);
        prescriptionItem.setQuantity(14);
        prescriptionItem.setDispensed(false);

        prescriptionItemDTO = new PrescriptionItemDTO();
        prescriptionItemDTO.setId(1L);
        prescriptionItemDTO.setPrescriptionId(1L);
        prescriptionItemDTO.setMedicationId(1L);
        prescriptionItemDTO.setMedicationName("Aspirin");
        prescriptionItemDTO.setDosage("500mg");
        prescriptionItemDTO.setFrequency("Twice daily");
        prescriptionItemDTO.setDurationDays(7);
        prescriptionItemDTO.setQuantity(14);
        prescriptionItemDTO.setDispensed(false);
    }

    @Test
    void shouldCreatePrescriptionItem() {
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));
        when(prescriptionItemMapper.toEntity(any(PrescriptionItemDTO.class))).thenReturn(prescriptionItem);
        when(prescriptionItemRepository.save(any(PrescriptionItem.class))).thenReturn(prescriptionItem);
        when(prescriptionItemMapper.toDTO(any(PrescriptionItem.class))).thenReturn(prescriptionItemDTO);

        PrescriptionItemDTO result = prescriptionItemService.createPrescriptionItem(prescriptionItemDTO);

        assertNotNull(result);
        assertEquals("Aspirin", result.getMedicationName());
        verify(prescriptionItemRepository, times(1)).save(any(PrescriptionItem.class));
    }

    @Test
    void shouldThrowExceptionWhenPrescriptionNotFoundOnCreate() {
        when(prescriptionRepository.findById(999L)).thenReturn(Optional.empty());
        prescriptionItemDTO.setPrescriptionId(999L);

        assertThrows(ResourceNotFoundException.class, 
            () -> prescriptionItemService.createPrescriptionItem(prescriptionItemDTO));
    }

    @Test
    void shouldThrowExceptionWhenMedicationNotFoundOnCreate() {
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
        when(medicationRepository.findById(999L)).thenReturn(Optional.empty());
        prescriptionItemDTO.setMedicationId(999L);

        assertThrows(ResourceNotFoundException.class, 
            () -> prescriptionItemService.createPrescriptionItem(prescriptionItemDTO));
    }

    @Test
    void shouldGetPrescriptionItemById() {
        when(prescriptionItemRepository.findById(1L)).thenReturn(Optional.of(prescriptionItem));
        when(prescriptionItemMapper.toDTO(any(PrescriptionItem.class))).thenReturn(prescriptionItemDTO);

        PrescriptionItemDTO result = prescriptionItemService.getPrescriptionItemById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrowExceptionWhenItemNotFound() {
        when(prescriptionItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> prescriptionItemService.getPrescriptionItemById(999L));
    }

    @Test
    void shouldGetItemsByPrescriptionId() {
        List<PrescriptionItem> items = Arrays.asList(prescriptionItem);
        when(prescriptionItemRepository.findByPrescriptionId(1L)).thenReturn(items);
        when(prescriptionItemMapper.toDTO(any(PrescriptionItem.class))).thenReturn(prescriptionItemDTO);

        List<PrescriptionItemDTO> result = prescriptionItemService.getItemsByPrescriptionId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetItemsByMedicationId() {
        List<PrescriptionItem> items = Arrays.asList(prescriptionItem);
        when(prescriptionItemRepository.findByMedicationId(1L)).thenReturn(items);
        when(prescriptionItemMapper.toDTO(any(PrescriptionItem.class))).thenReturn(prescriptionItemDTO);

        List<PrescriptionItemDTO> result = prescriptionItemService.getItemsByMedicationId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldUpdatePrescriptionItem() {
        when(prescriptionItemRepository.findById(1L)).thenReturn(Optional.of(prescriptionItem));
        when(prescriptionItemRepository.save(any(PrescriptionItem.class))).thenReturn(prescriptionItem);
        when(prescriptionItemMapper.toDTO(any(PrescriptionItem.class))).thenReturn(prescriptionItemDTO);

        prescriptionItemDTO.setDosage("1000mg");
        PrescriptionItemDTO result = prescriptionItemService.updatePrescriptionItem(1L, prescriptionItemDTO);

        assertNotNull(result);
        verify(prescriptionItemRepository, times(1)).save(any(PrescriptionItem.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentItem() {
        when(prescriptionItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> prescriptionItemService.updatePrescriptionItem(999L, prescriptionItemDTO));
    }

    @Test
    void shouldDeletePrescriptionItem() {
        when(prescriptionItemRepository.existsById(1L)).thenReturn(true);
        doNothing().when(prescriptionItemRepository).deleteById(1L);

        prescriptionItemService.deletePrescriptionItem(1L);

        verify(prescriptionItemRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentItem() {
        when(prescriptionItemRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, 
            () -> prescriptionItemService.deletePrescriptionItem(999L));
    }

    @Test
    void shouldDispenseItem() {
        when(prescriptionItemRepository.findById(1L)).thenReturn(Optional.of(prescriptionItem));
        when(pharmacistRepository.findById(1L)).thenReturn(Optional.of(pharmacist));
        when(prescriptionItemRepository.save(any(PrescriptionItem.class))).thenReturn(prescriptionItem);
        when(prescriptionItemMapper.toDTO(any(PrescriptionItem.class))).thenReturn(prescriptionItemDTO);

        PrescriptionItemDTO result = prescriptionItemService.dispenseItem(1L, 1L);

        assertNotNull(result);
        verify(prescriptionItemRepository, times(1)).save(any(PrescriptionItem.class));
    }

    @Test
    void shouldThrowExceptionWhenPharmacistNotFoundOnDispense() {
        when(prescriptionItemRepository.findById(1L)).thenReturn(Optional.of(prescriptionItem));
        when(pharmacistRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> prescriptionItemService.dispenseItem(1L, 999L));
    }

    @Test
    void shouldGetUndispensedItems() {
        List<PrescriptionItem> items = Arrays.asList(prescriptionItem);
        when(prescriptionItemRepository.findUndispensedByPrescriptionId(1L)).thenReturn(items);
        when(prescriptionItemMapper.toDTO(any(PrescriptionItem.class))).thenReturn(prescriptionItemDTO);

        List<PrescriptionItemDTO> result = prescriptionItemService.getUndispensedItemsByPrescriptionId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetDispensedItemsByPharmacist() {
        List<PrescriptionItem> items = Arrays.asList(prescriptionItem);
        when(prescriptionItemRepository.findDispensedByPharmacistId(1L)).thenReturn(items);
        when(prescriptionItemMapper.toDTO(any(PrescriptionItem.class))).thenReturn(prescriptionItemDTO);

        List<PrescriptionItemDTO> result = prescriptionItemService.getDispensedItemsByPharmacistId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
