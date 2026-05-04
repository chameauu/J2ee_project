package com.hospital.management.services;

import com.hospital.management.dto.MedicationDTO;
import com.hospital.management.entities.Medication;
import com.hospital.management.enums.MedicationType;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.MedicationMapper;
import com.hospital.management.repositories.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicationServiceImpl implements IMedicationService {

    private final MedicationRepository medicationRepository;
    private final MedicationMapper medicationMapper;

    @Override
    @Transactional
    public MedicationDTO createMedication(MedicationDTO dto) {
        Medication medication = medicationMapper.toEntity(dto);
        Medication saved = medicationRepository.save(medication);
        return medicationMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicationDTO getMedicationById(Long id) {
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + id));
        return medicationMapper.toDTO(medication);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationDTO> getAllMedications() {
        return medicationRepository.findAll().stream()
                .map(medicationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MedicationDTO updateMedication(Long id, MedicationDTO dto) {
        Medication existing = medicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + id));

        existing.setName(dto.getName());
        existing.setGenericName(dto.getGenericName());
        existing.setManufacturer(dto.getManufacturer());
        existing.setType(dto.getType());
        existing.setStrength(dto.getStrength());
        existing.setDescription(dto.getDescription());

        Medication updated = medicationRepository.save(existing);
        return medicationMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteMedication(Long id) {
        if (!medicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Medication not found with id: " + id);
        }
        medicationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationDTO> searchMedications(String keyword) {
        return medicationRepository.searchMedications(keyword).stream()
                .map(medicationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationDTO> getMedicationsByType(MedicationType type) {
        return medicationRepository.findByType(type).stream()
                .map(medicationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationDTO> getMedicationsByManufacturer(String manufacturer) {
        return medicationRepository.findByManufacturer(manufacturer).stream()
                .map(medicationMapper::toDTO)
                .collect(Collectors.toList());
    }
}
