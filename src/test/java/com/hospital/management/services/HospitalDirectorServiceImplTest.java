package com.hospital.management.services;

import com.hospital.management.dto.HospitalDirectorDTO;
import com.hospital.management.entities.HospitalDirector;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.HospitalDirectorMapper;
import com.hospital.management.repositories.HospitalDirectorRepository;
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
class HospitalDirectorServiceImplTest {

    @Mock
    private HospitalDirectorRepository hospitalDirectorRepository;

    @Mock
    private HospitalDirectorMapper hospitalDirectorMapper;

    @InjectMocks
    private HospitalDirectorServiceImpl hospitalDirectorService;

    private HospitalDirector hospitalDirector;
    private HospitalDirectorDTO hospitalDirectorDTO;

    @BeforeEach
    void setUp() {
        hospitalDirector = new HospitalDirector();
        hospitalDirector.setId(1L);
        hospitalDirector.setFirstName("John");
        hospitalDirector.setLastName("Director");
        hospitalDirector.setEmail("john.director@hospital.com");
        hospitalDirector.setPhone("1234567890");
        hospitalDirector.setHospitalName("City Hospital");
        hospitalDirector.setAppointmentDate(LocalDate.of(2020, 1, 1));
        hospitalDirector.setCredentials("MD, MBA, FACHE");
        hospitalDirector.setActive(true);

        hospitalDirectorDTO = new HospitalDirectorDTO();
        hospitalDirectorDTO.setId(1L);
        hospitalDirectorDTO.setFirstName("John");
        hospitalDirectorDTO.setLastName("Director");
        hospitalDirectorDTO.setEmail("john.director@hospital.com");
        hospitalDirectorDTO.setPhone("1234567890");
        hospitalDirectorDTO.setHospitalName("City Hospital");
        hospitalDirectorDTO.setAppointmentDate(LocalDate.of(2020, 1, 1));
        hospitalDirectorDTO.setCredentials("MD, MBA, FACHE");
        hospitalDirectorDTO.setActive(true);
    }

    @Test
    void createHospitalDirector_Success() {
        when(hospitalDirectorMapper.toEntity(any(HospitalDirectorDTO.class))).thenReturn(hospitalDirector);
        when(hospitalDirectorRepository.save(any(HospitalDirector.class))).thenReturn(hospitalDirector);
        when(hospitalDirectorMapper.toDTO(any(HospitalDirector.class))).thenReturn(hospitalDirectorDTO);

        HospitalDirectorDTO result = hospitalDirectorService.createHospitalDirector(hospitalDirectorDTO);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Director", result.getLastName());
        assertEquals("john.director@hospital.com", result.getEmail());
        verify(hospitalDirectorRepository, times(1)).save(any(HospitalDirector.class));
    }

    @Test
    void getHospitalDirectorById_Success() {
        when(hospitalDirectorRepository.findById(1L)).thenReturn(Optional.of(hospitalDirector));
        when(hospitalDirectorMapper.toDTO(any(HospitalDirector.class))).thenReturn(hospitalDirectorDTO);

        HospitalDirectorDTO result = hospitalDirectorService.getHospitalDirectorById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        verify(hospitalDirectorRepository, times(1)).findById(1L);
    }

    @Test
    void getHospitalDirectorById_NotFound() {
        when(hospitalDirectorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            hospitalDirectorService.getHospitalDirectorById(999L);
        });

        verify(hospitalDirectorRepository, times(1)).findById(999L);
    }

    @Test
    void getAllHospitalDirectors_Success() {
        HospitalDirector director2 = new HospitalDirector();
        director2.setId(2L);
        director2.setFirstName("Jane");
        director2.setLastName("Director");

        HospitalDirectorDTO dto2 = new HospitalDirectorDTO();
        dto2.setId(2L);
        dto2.setFirstName("Jane");
        dto2.setLastName("Director");

        when(hospitalDirectorRepository.findAll()).thenReturn(Arrays.asList(hospitalDirector, director2));
        when(hospitalDirectorMapper.toDTO(hospitalDirector)).thenReturn(hospitalDirectorDTO);
        when(hospitalDirectorMapper.toDTO(director2)).thenReturn(dto2);

        List<HospitalDirectorDTO> result = hospitalDirectorService.getAllHospitalDirectors();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(hospitalDirectorRepository, times(1)).findAll();
    }

    @Test
    void updateHospitalDirector_Success() {
        HospitalDirectorDTO updateDTO = new HospitalDirectorDTO();
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("Director");
        updateDTO.setEmail("updated@hospital.com");
        updateDTO.setPhone("9876543210");
        updateDTO.setHospitalName("Updated Hospital");
        updateDTO.setAppointmentDate(LocalDate.of(2021, 1, 1));
        updateDTO.setCredentials("MD, PhD");
        updateDTO.setActive(false);

        when(hospitalDirectorRepository.findById(1L)).thenReturn(Optional.of(hospitalDirector));
        when(hospitalDirectorRepository.save(any(HospitalDirector.class))).thenReturn(hospitalDirector);
        when(hospitalDirectorMapper.toDTO(any(HospitalDirector.class))).thenReturn(updateDTO);

        HospitalDirectorDTO result = hospitalDirectorService.updateHospitalDirector(1L, updateDTO);

        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        verify(hospitalDirectorRepository, times(1)).findById(1L);
        verify(hospitalDirectorRepository, times(1)).save(any(HospitalDirector.class));
    }

    @Test
    void updateHospitalDirector_NotFound() {
        when(hospitalDirectorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            hospitalDirectorService.updateHospitalDirector(999L, hospitalDirectorDTO);
        });

        verify(hospitalDirectorRepository, times(1)).findById(999L);
        verify(hospitalDirectorRepository, never()).save(any(HospitalDirector.class));
    }

    @Test
    void deleteHospitalDirector_Success() {
        when(hospitalDirectorRepository.existsById(1L)).thenReturn(true);
        doNothing().when(hospitalDirectorRepository).deleteById(1L);

        hospitalDirectorService.deleteHospitalDirector(1L);

        verify(hospitalDirectorRepository, times(1)).existsById(1L);
        verify(hospitalDirectorRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteHospitalDirector_NotFound() {
        when(hospitalDirectorRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            hospitalDirectorService.deleteHospitalDirector(999L);
        });

        verify(hospitalDirectorRepository, times(1)).existsById(999L);
        verify(hospitalDirectorRepository, never()).deleteById(anyLong());
    }
}
