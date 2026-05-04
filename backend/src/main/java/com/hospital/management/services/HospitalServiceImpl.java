package com.hospital.management.services;

import com.hospital.management.dto.HospitalDTO;
import com.hospital.management.entities.Hospital;
import com.hospital.management.exceptions.DuplicateResourceException;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.HospitalMapper;
import com.hospital.management.repositories.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HospitalServiceImpl implements IHospitalService {

    private final HospitalRepository hospitalRepository;
    private final HospitalMapper hospitalMapper;

    @Override
    public HospitalDTO createHospital(HospitalDTO hospitalDTO) {
        // Check if registration number already exists
        if (hospitalRepository.existsByRegistrationNumber(hospitalDTO.getRegistrationNumber())) {
            throw new DuplicateResourceException(
                    "Hospital with registration number " + hospitalDTO.getRegistrationNumber() + " already exists"
            );
        }

        Hospital hospital = hospitalMapper.toEntity(hospitalDTO);
        Hospital savedHospital = hospitalRepository.save(hospital);
        return hospitalMapper.toDTO(savedHospital);
    }

    @Override
    @Transactional(readOnly = true)
    public HospitalDTO getHospitalById(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + id));
        return hospitalMapper.toDTO(hospital);
    }

    @Override
    public HospitalDTO updateHospital(Long id, HospitalDTO hospitalDTO) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + id));

        // Check if registration number is being changed and if it already exists
        if (!hospital.getRegistrationNumber().equals(hospitalDTO.getRegistrationNumber())) {
            if (hospitalRepository.existsByRegistrationNumber(hospitalDTO.getRegistrationNumber())) {
                throw new DuplicateResourceException(
                        "Hospital with registration number " + hospitalDTO.getRegistrationNumber() + " already exists"
                );
            }
        }

        hospitalMapper.updateEntityFromDTO(hospitalDTO, hospital);
        Hospital updatedHospital = hospitalRepository.save(hospital);
        return hospitalMapper.toDTO(updatedHospital);
    }

    @Override
    public void deleteHospital(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + id));
        hospitalRepository.delete(hospital);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HospitalDTO> getAllHospitals() {
        return hospitalRepository.findAll().stream()
                .map(hospitalMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HospitalDTO> searchHospitals(String keyword) {
        return hospitalRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(hospitalMapper::toDTO)
                .collect(Collectors.toList());
    }
}
