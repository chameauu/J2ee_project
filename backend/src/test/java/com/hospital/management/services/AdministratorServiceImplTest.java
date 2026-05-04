package com.hospital.management.services;

import com.hospital.management.dto.AdministratorDTO;
import com.hospital.management.entities.Administrator;
import com.hospital.management.exceptions.DuplicateResourceException;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.AdministratorMapper;
import com.hospital.management.repositories.AdministratorRepository;
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
class AdministratorServiceImplTest {

    @Mock
    private AdministratorRepository administratorRepository;

    @Mock
    private AdministratorMapper administratorMapper;

    @InjectMocks
    private AdministratorServiceImpl administratorService;

    private Administrator administrator;
    private AdministratorDTO administratorDTO;

    @BeforeEach
    void setUp() {
        administrator = new Administrator();
        administrator.setId(1L);
        administrator.setFirstName("John");
        administrator.setLastName("Admin");
        administrator.setEmail("john.admin@hospital.com");
        administrator.setPhone("1234567890");
        administrator.setDepartment("IT");
        administrator.setAccessLevel("FULL");

        administratorDTO = new AdministratorDTO();
        administratorDTO.setId(1L);
        administratorDTO.setFirstName("John");
        administratorDTO.setLastName("Admin");
        administratorDTO.setEmail("john.admin@hospital.com");
        administratorDTO.setPhone("1234567890");
        administratorDTO.setDepartment("IT");
        administratorDTO.setAccessLevel("FULL");
    }

    @Test
    void shouldCreateAdministrator() {
        when(administratorRepository.existsByEmail(administratorDTO.getEmail())).thenReturn(false);
        when(administratorMapper.toEntity(administratorDTO)).thenReturn(administrator);
        when(administratorRepository.save(administrator)).thenReturn(administrator);
        when(administratorMapper.toDTO(administrator)).thenReturn(administratorDTO);

        AdministratorDTO result = administratorService.createAdministrator(administratorDTO);

        assertNotNull(result);
        assertEquals(administratorDTO.getEmail(), result.getEmail());
        verify(administratorRepository).save(administrator);
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        when(administratorRepository.existsByEmail(administratorDTO.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            administratorService.createAdministrator(administratorDTO);
        });

        verify(administratorRepository, never()).save(any());
    }

    @Test
    void shouldGetAdministratorById() {
        when(administratorRepository.findById(1L)).thenReturn(Optional.of(administrator));
        when(administratorMapper.toDTO(administrator)).thenReturn(administratorDTO);

        AdministratorDTO result = administratorService.getAdministratorById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john.admin@hospital.com", result.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenAdministratorNotFound() {
        when(administratorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            administratorService.getAdministratorById(1L);
        });
    }

    @Test
    void shouldUpdateAdministrator() {
        when(administratorRepository.findById(1L)).thenReturn(Optional.of(administrator));
        when(administratorRepository.save(administrator)).thenReturn(administrator);
        when(administratorMapper.toDTO(administrator)).thenReturn(administratorDTO);

        AdministratorDTO result = administratorService.updateAdministrator(1L, administratorDTO);

        assertNotNull(result);
        assertEquals(administratorDTO.getEmail(), result.getEmail());
        verify(administratorRepository).save(administrator);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentAdministrator() {
        when(administratorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            administratorService.updateAdministrator(1L, administratorDTO);
        });

        verify(administratorRepository, never()).save(any());
    }

    @Test
    void shouldDeleteAdministrator() {
        when(administratorRepository.existsById(1L)).thenReturn(true);

        administratorService.deleteAdministrator(1L);

        verify(administratorRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentAdministrator() {
        when(administratorRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            administratorService.deleteAdministrator(1L);
        });

        verify(administratorRepository, never()).deleteById(any());
    }

    @Test
    void shouldGetAllAdministrators() {
        List<Administrator> administrators = Arrays.asList(administrator);
        when(administratorRepository.findAll()).thenReturn(administrators);
        when(administratorMapper.toDTO(administrator)).thenReturn(administratorDTO);

        List<AdministratorDTO> result = administratorService.getAllAdministrators();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("john.admin@hospital.com", result.get(0).getEmail());
    }
}
