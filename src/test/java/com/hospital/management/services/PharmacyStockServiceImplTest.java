package com.hospital.management.services;

import com.hospital.management.dto.PharmacyStockDTO;
import com.hospital.management.entities.Medication;
import com.hospital.management.entities.PharmacyStock;
import com.hospital.management.enums.MedicationType;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.PharmacyStockMapper;
import com.hospital.management.repositories.MedicationRepository;
import com.hospital.management.repositories.PharmacyStockRepository;
import org.junit.jupiter.api.BeforeEach;
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
class PharmacyStockServiceImplTest {

    @Mock
    private PharmacyStockRepository pharmacyStockRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private PharmacyStockMapper pharmacyStockMapper;

    @InjectMocks
    private PharmacyStockServiceImpl pharmacyStockService;

    private Medication medication;
    private PharmacyStock pharmacyStock;
    private PharmacyStockDTO pharmacyStockDTO;

    @BeforeEach
    void setUp() {
        medication = new Medication();
        medication.setId(1L);
        medication.setName("Aspirin");
        medication.setType(MedicationType.TABLET);

        pharmacyStock = new PharmacyStock();
        pharmacyStock.setId(1L);
        pharmacyStock.setMedication(medication);
        pharmacyStock.setQuantity(100);
        pharmacyStock.setReorderLevel(20);
        pharmacyStock.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStock.setBatchNumber("BATCH001");
        pharmacyStock.setUnitPrice(5.0);

        pharmacyStockDTO = new PharmacyStockDTO();
        pharmacyStockDTO.setId(1L);
        pharmacyStockDTO.setMedicationId(1L);
        pharmacyStockDTO.setQuantity(100);
        pharmacyStockDTO.setReorderLevel(20);
        pharmacyStockDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        pharmacyStockDTO.setBatchNumber("BATCH001");
        pharmacyStockDTO.setUnitPrice(5.0);
    }

    @Test
    void shouldCreateStock() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));
        when(pharmacyStockMapper.toEntity(any(PharmacyStockDTO.class))).thenReturn(pharmacyStock);
        when(pharmacyStockRepository.save(any(PharmacyStock.class))).thenReturn(pharmacyStock);
        when(pharmacyStockMapper.toDTO(any(PharmacyStock.class))).thenReturn(pharmacyStockDTO);

        PharmacyStockDTO result = pharmacyStockService.createStock(pharmacyStockDTO);

        assertNotNull(result);
        assertEquals(100, result.getQuantity());
        verify(pharmacyStockRepository, times(1)).save(any(PharmacyStock.class));
    }

    @Test
    void shouldThrowExceptionWhenMedicationNotFoundOnCreate() {
        when(medicationRepository.findById(999L)).thenReturn(Optional.empty());
        pharmacyStockDTO.setMedicationId(999L);

        assertThrows(ResourceNotFoundException.class, () -> pharmacyStockService.createStock(pharmacyStockDTO));
    }

    @Test
    void shouldGetStockById() {
        when(pharmacyStockRepository.findById(1L)).thenReturn(Optional.of(pharmacyStock));
        when(pharmacyStockMapper.toDTO(any(PharmacyStock.class))).thenReturn(pharmacyStockDTO);

        PharmacyStockDTO result = pharmacyStockService.getStockById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrowExceptionWhenStockNotFound() {
        when(pharmacyStockRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pharmacyStockService.getStockById(999L));
    }

    @Test
    void shouldGetAllStock() {
        List<PharmacyStock> stocks = Arrays.asList(pharmacyStock);
        when(pharmacyStockRepository.findAll()).thenReturn(stocks);
        when(pharmacyStockMapper.toDTO(any(PharmacyStock.class))).thenReturn(pharmacyStockDTO);

        List<PharmacyStockDTO> result = pharmacyStockService.getAllStock();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldUpdateStock() {
        when(pharmacyStockRepository.findById(1L)).thenReturn(Optional.of(pharmacyStock));
        when(pharmacyStockRepository.save(any(PharmacyStock.class))).thenReturn(pharmacyStock);
        when(pharmacyStockMapper.toDTO(any(PharmacyStock.class))).thenReturn(pharmacyStockDTO);

        pharmacyStockDTO.setQuantity(150);
        PharmacyStockDTO result = pharmacyStockService.updateStock(1L, pharmacyStockDTO);

        assertNotNull(result);
        verify(pharmacyStockRepository, times(1)).save(any(PharmacyStock.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentStock() {
        when(pharmacyStockRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pharmacyStockService.updateStock(999L, pharmacyStockDTO));
    }

    @Test
    void shouldDeleteStock() {
        when(pharmacyStockRepository.existsById(1L)).thenReturn(true);
        doNothing().when(pharmacyStockRepository).deleteById(1L);

        pharmacyStockService.deleteStock(1L);

        verify(pharmacyStockRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentStock() {
        when(pharmacyStockRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> pharmacyStockService.deleteStock(999L));
    }

    @Test
    void shouldGetStockByMedicationId() {
        List<PharmacyStock> stocks = Arrays.asList(pharmacyStock);
        when(pharmacyStockRepository.findByMedicationId(1L)).thenReturn(stocks);
        when(pharmacyStockMapper.toDTO(any(PharmacyStock.class))).thenReturn(pharmacyStockDTO);

        List<PharmacyStockDTO> result = pharmacyStockService.getStockByMedicationId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetLowStockItems() {
        List<PharmacyStock> stocks = Arrays.asList(pharmacyStock);
        when(pharmacyStockRepository.findLowStockItems()).thenReturn(stocks);
        when(pharmacyStockMapper.toDTO(any(PharmacyStock.class))).thenReturn(pharmacyStockDTO);

        List<PharmacyStockDTO> result = pharmacyStockService.getLowStockItems();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetExpiringSoonItems() {
        List<PharmacyStock> stocks = Arrays.asList(pharmacyStock);
        when(pharmacyStockRepository.findExpiringSoon(any(LocalDate.class))).thenReturn(stocks);
        when(pharmacyStockMapper.toDTO(any(PharmacyStock.class))).thenReturn(pharmacyStockDTO);

        List<PharmacyStockDTO> result = pharmacyStockService.getExpiringSoonItems();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetExpiredStock() {
        List<PharmacyStock> stocks = Arrays.asList(pharmacyStock);
        when(pharmacyStockRepository.findExpiredStock(any(LocalDate.class))).thenReturn(stocks);
        when(pharmacyStockMapper.toDTO(any(PharmacyStock.class))).thenReturn(pharmacyStockDTO);

        List<PharmacyStockDTO> result = pharmacyStockService.getExpiredStock();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldReduceStock() {
        when(pharmacyStockRepository.findById(1L)).thenReturn(Optional.of(pharmacyStock));
        when(pharmacyStockRepository.save(any(PharmacyStock.class))).thenReturn(pharmacyStock);
        when(pharmacyStockMapper.toDTO(any(PharmacyStock.class))).thenReturn(pharmacyStockDTO);

        PharmacyStockDTO result = pharmacyStockService.reduceStock(1L, 10);

        assertNotNull(result);
        verify(pharmacyStockRepository, times(1)).save(any(PharmacyStock.class));
    }

    @Test
    void shouldAddStock() {
        when(pharmacyStockRepository.findById(1L)).thenReturn(Optional.of(pharmacyStock));
        when(pharmacyStockRepository.save(any(PharmacyStock.class))).thenReturn(pharmacyStock);
        when(pharmacyStockMapper.toDTO(any(PharmacyStock.class))).thenReturn(pharmacyStockDTO);

        PharmacyStockDTO result = pharmacyStockService.addStock(1L, 50);

        assertNotNull(result);
        verify(pharmacyStockRepository, times(1)).save(any(PharmacyStock.class));
    }
}
