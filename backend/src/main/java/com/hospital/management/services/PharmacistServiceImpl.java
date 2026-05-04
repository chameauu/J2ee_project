package com.hospital.management.services;

import com.hospital.management.dto.PharmacistDTO;
import com.hospital.management.entities.Hospital;
import com.hospital.management.entities.Pharmacist;
import com.hospital.management.exceptions.DuplicateResourceException;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.PharmacistMapper;
import com.hospital.management.repositories.HospitalRepository;
import com.hospital.management.repositories.PharmacistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacistServiceImpl implements IPharmacistService {

    private final PharmacistRepository pharmacistRepository;
    private final PharmacistMapper pharmacistMapper;
    private final HospitalRepository hospitalRepository;

    @Override
    public PharmacistDTO createPharmacist(PharmacistDTO dto) {
        if (pharmacistRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
        }
        if (pharmacistRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new DuplicateResourceException("License number already exists: " + dto.getLicenseNumber());
        }

        Pharmacist pharmacist = pharmacistMapper.toEntity(dto);
        
        // Assign hospital if hospitalId is provided (Phase 10.2)
        if (dto.getHospitalId() != null) {
            Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Hospital not found with id: " + dto.getHospitalId()));
            pharmacist.setHospital(hospital);
        }
        
        Pharmacist saved = pharmacistRepository.save(pharmacist);
        return pharmacistMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacistDTO getPharmacistById(Long id) {
        Pharmacist pharmacist = pharmacistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacist not found with id: " + id));
        return pharmacistMapper.toDTO(pharmacist);
    }

    @Override
    public PharmacistDTO updatePharmacist(Long id, PharmacistDTO dto) {
        Pharmacist pharmacist = pharmacistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacist not found with id: " + id));

        pharmacist.setFirstName(dto.getFirstName());
        pharmacist.setLastName(dto.getLastName());
        pharmacist.setEmail(dto.getEmail());
        pharmacist.setPhone(dto.getPhone());
        pharmacist.setLicenseNumber(dto.getLicenseNumber());
        pharmacist.setQualification(dto.getQualification());

        // Update hospital if hospitalId is provided (Phase 10.2)
        if (dto.getHospitalId() != null) {
            Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Hospital not found with id: " + dto.getHospitalId()));
            pharmacist.setHospital(hospital);
        }

        Pharmacist updated = pharmacistRepository.save(pharmacist);
        return pharmacistMapper.toDTO(updated);
    }

    @Override
    public void deletePharmacist(Long id) {
        Pharmacist pharmacist = pharmacistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacist not found with id: " + id));
        pharmacistRepository.delete(pharmacist);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacistDTO> getAllPharmacists() {
        return pharmacistRepository.findAll().stream()
                .map(pharmacistMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Hospital-scoped queries (Phase 10.3)
    @Override
    @Transactional(readOnly = true)
    public List<PharmacistDTO> getPharmacistsByHospital(Long hospitalId) {
        // Validate hospital exists
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital not found with id: " + hospitalId);
        }
        
        return pharmacistRepository.findByHospitalId(hospitalId).stream()
                .map(pharmacistMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countPharmacistsByHospital(Long hospitalId) {
        // Validate hospital exists
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital not found with id: " + hospitalId);
        }
        
        return pharmacistRepository.countByHospitalId(hospitalId);
    }
}
