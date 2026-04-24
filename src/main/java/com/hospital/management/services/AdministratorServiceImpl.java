package com.hospital.management.services;

import com.hospital.management.dto.AdministratorDTO;
import com.hospital.management.entities.Administrator;
import com.hospital.management.exceptions.DuplicateResourceException;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.AdministratorMapper;
import com.hospital.management.repositories.AdministratorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdministratorServiceImpl implements IAdministratorService {

    private final AdministratorRepository administratorRepository;
    private final AdministratorMapper administratorMapper;

    @Override
    public AdministratorDTO createAdministrator(AdministratorDTO dto) {
        if (administratorRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Administrator with email " + dto.getEmail() + " already exists");
        }

        Administrator administrator = administratorMapper.toEntity(dto);
        Administrator saved = administratorRepository.save(administrator);
        return administratorMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AdministratorDTO getAdministratorById(Long id) {
        Administrator administrator = administratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Administrator not found with id: " + id));
        return administratorMapper.toDTO(administrator);
    }

    @Override
    public AdministratorDTO updateAdministrator(Long id, AdministratorDTO dto) {
        Administrator administrator = administratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Administrator not found with id: " + id));

        administrator.setFirstName(dto.getFirstName());
        administrator.setLastName(dto.getLastName());
        administrator.setEmail(dto.getEmail());
        administrator.setPhone(dto.getPhone());
        administrator.setDepartment(dto.getDepartment());
        administrator.setAccessLevel(dto.getAccessLevel());

        Administrator updated = administratorRepository.save(administrator);
        return administratorMapper.toDTO(updated);
    }

    @Override
    public void deleteAdministrator(Long id) {
        if (!administratorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Administrator not found with id: " + id);
        }
        administratorRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdministratorDTO> getAllAdministrators() {
        return administratorRepository.findAll().stream()
                .map(administratorMapper::toDTO)
                .collect(Collectors.toList());
    }
}
