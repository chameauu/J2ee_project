package com.hospital.management.services;

import com.hospital.management.dto.AdministratorDTO;
import com.hospital.management.entities.Administrator;
import com.hospital.management.entities.Hospital;
import com.hospital.management.exceptions.DuplicateResourceException;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.AdministratorMapper;
import com.hospital.management.repositories.AdministratorRepository;
import com.hospital.management.repositories.HospitalRepository;
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
    private final HospitalRepository hospitalRepository;

    @Override
    public AdministratorDTO createAdministrator(AdministratorDTO dto) {
        if (administratorRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Administrator with email " + dto.getEmail() + " already exists");
        }

        Administrator administrator = administratorMapper.toEntity(dto);
        
        // Assign hospital if hospitalId is provided (Phase 10.2)
        if (dto.getHospitalId() != null) {
            Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Hospital not found with id: " + dto.getHospitalId()));
            administrator.setHospital(hospital);
        }
        
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

        // Update hospital if hospitalId is provided (Phase 10.2)
        if (dto.getHospitalId() != null) {
            Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Hospital not found with id: " + dto.getHospitalId()));
            administrator.setHospital(hospital);
        }

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

    // Phase 10.3: Hospital-scoped queries
    @Override
    @Transactional(readOnly = true)
    public List<AdministratorDTO> getAdministratorsByHospital(Long hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital not found with id: " + hospitalId);
        }
        return administratorRepository.findByHospitalId(hospitalId).stream()
                .map(administratorMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAdministratorsByHospital(Long hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital not found with id: " + hospitalId);
        }
        return administratorRepository.countByHospitalId(hospitalId);
    }
}
