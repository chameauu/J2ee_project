package com.hospital.management.services;

import com.hospital.management.dto.HospitalDTO;
import com.hospital.management.entities.Hospital;
import com.hospital.management.exceptions.DuplicateResourceException;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.HospitalMapper;
import com.hospital.management.repositories.HospitalRepository;
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
class HospitalServiceImplTest {

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private HospitalMapper hospitalMapper;

    @InjectMocks
    private HospitalServiceImpl hospitalService;

    private Hospital hospital;
    private HospitalDTO hospitalDTO;

    @BeforeEach
    void setUp() {
        hospital = new Hospital();
        hospital.setId(1L);
        hospital.setName("City General Hospital");
        hospital.setAddress("123 Main Street");
        hospital.setPhone("+1-555-0100");
        hospital.setEmail("info@cityhospital.com");
        hospital.setRegistrationNumber("REG-2024-001");
        hospital.setEstablishedDate(LocalDate.of(1990, 1, 15));

        hospitalDTO = new HospitalDTO();
        hospitalDTO.setId(1L);
        hospitalDTO.setName("City General Hospital");
        hospitalDTO.setAddress("123 Main Street");
        hospitalDTO.setPhone("+1-555-0100");
        hospitalDTO.setEmail("info@cityhospital.com");
        hospitalDTO.setRegistrationNumber("REG-2024-001");
        hospitalDTO.setEstablishedDate(LocalDate.of(1990, 1, 15));
    }

    @Test
    void shouldCreateHospital() {
        when(hospitalRepository.existsByRegistrationNumber(hospitalDTO.getRegistrationNumber())).thenReturn(false);
        when(hospitalMapper.toEntity(hospitalDTO)).thenReturn(hospital);
        when(hospitalRepository.save(hospital)).thenReturn(hospital);
        when(hospitalMapper.toDTO(hospital)).thenReturn(hospitalDTO);

        HospitalDTO result = hospitalService.createHospital(hospitalDTO);

        assertNotNull(result);
        assertEquals("City General Hospital", result.getName());
        assertEquals("REG-2024-001", result.getRegistrationNumber());
        verify(hospitalRepository).existsByRegistrationNumber(hospitalDTO.getRegistrationNumber());
        verify(hospitalRepository).save(hospital);
    }

    @Test
    void shouldThrowExceptionWhenRegistrationNumberExists() {
        when(hospitalRepository.existsByRegistrationNumber(hospitalDTO.getRegistrationNumber())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            hospitalService.createHospital(hospitalDTO);
        });

        verify(hospitalRepository).existsByRegistrationNumber(hospitalDTO.getRegistrationNumber());
        verify(hospitalRepository, never()).save(any());
    }

    @Test
    void shouldGetHospitalById() {
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(hospitalMapper.toDTO(hospital)).thenReturn(hospitalDTO);

        HospitalDTO result = hospitalService.getHospitalById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("City General Hospital", result.getName());
        verify(hospitalRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenHospitalNotFound() {
        when(hospitalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            hospitalService.getHospitalById(99L);
        });

        verify(hospitalRepository).findById(99L);
    }

    @Test
    void shouldUpdateHospital() {
        HospitalDTO updateDTO = new HospitalDTO();
        updateDTO.setName("City General Hospital - Updated");
        updateDTO.setAddress("123 Main Street");
        updateDTO.setPhone("+1-555-0101");
        updateDTO.setEmail("contact@cityhospital.com");
        updateDTO.setRegistrationNumber("REG-2024-001");
        updateDTO.setEstablishedDate(LocalDate.of(1990, 1, 15));

        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(hospitalRepository.save(hospital)).thenReturn(hospital);
        when(hospitalMapper.toDTO(hospital)).thenReturn(updateDTO);

        HospitalDTO result = hospitalService.updateHospital(1L, updateDTO);

        assertNotNull(result);
        assertEquals("City General Hospital - Updated", result.getName());
        verify(hospitalRepository).findById(1L);
        verify(hospitalMapper).updateEntityFromDTO(updateDTO, hospital);
        verify(hospitalRepository).save(hospital);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentHospital() {
        when(hospitalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            hospitalService.updateHospital(99L, hospitalDTO);
        });

        verify(hospitalRepository).findById(99L);
        verify(hospitalRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithDuplicateRegistrationNumber() {
        HospitalDTO updateDTO = new HospitalDTO();
        updateDTO.setName("City General Hospital");
        updateDTO.setAddress("123 Main Street");
        updateDTO.setPhone("+1-555-0100");
        updateDTO.setEmail("info@cityhospital.com");
        updateDTO.setRegistrationNumber("REG-2024-999"); // Different registration number
        updateDTO.setEstablishedDate(LocalDate.of(1990, 1, 15));

        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(hospitalRepository.existsByRegistrationNumber("REG-2024-999")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            hospitalService.updateHospital(1L, updateDTO);
        });

        verify(hospitalRepository).findById(1L);
        verify(hospitalRepository).existsByRegistrationNumber("REG-2024-999");
        verify(hospitalRepository, never()).save(any());
    }

    @Test
    void shouldDeleteHospital() {
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        doNothing().when(hospitalRepository).delete(hospital);

        hospitalService.deleteHospital(1L);

        verify(hospitalRepository).findById(1L);
        verify(hospitalRepository).delete(hospital);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentHospital() {
        when(hospitalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            hospitalService.deleteHospital(99L);
        });

        verify(hospitalRepository).findById(99L);
        verify(hospitalRepository, never()).delete(any());
    }

    @Test
    void shouldGetAllHospitals() {
        Hospital hospital2 = new Hospital();
        hospital2.setId(2L);
        hospital2.setName("County Medical Center");
        hospital2.setRegistrationNumber("REG-2024-002");

        HospitalDTO hospitalDTO2 = new HospitalDTO();
        hospitalDTO2.setId(2L);
        hospitalDTO2.setName("County Medical Center");
        hospitalDTO2.setRegistrationNumber("REG-2024-002");

        List<Hospital> hospitals = Arrays.asList(hospital, hospital2);

        when(hospitalRepository.findAll()).thenReturn(hospitals);
        when(hospitalMapper.toDTO(hospital)).thenReturn(hospitalDTO);
        when(hospitalMapper.toDTO(hospital2)).thenReturn(hospitalDTO2);

        List<HospitalDTO> result = hospitalService.getAllHospitals();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("City General Hospital", result.get(0).getName());
        assertEquals("County Medical Center", result.get(1).getName());
        verify(hospitalRepository).findAll();
    }

    @Test
    void shouldSearchHospitalsByName() {
        List<Hospital> hospitals = Arrays.asList(hospital);

        when(hospitalRepository.findByNameContainingIgnoreCase("City")).thenReturn(hospitals);
        when(hospitalMapper.toDTO(hospital)).thenReturn(hospitalDTO);

        List<HospitalDTO> result = hospitalService.searchHospitals("City");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("City General Hospital", result.get(0).getName());
        verify(hospitalRepository).findByNameContainingIgnoreCase("City");
    }
}
