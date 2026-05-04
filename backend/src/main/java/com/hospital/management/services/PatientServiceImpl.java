package com.hospital.management.services;

import com.hospital.management.dto.PatientDTO;
import com.hospital.management.entities.Hospital;
import com.hospital.management.entities.Patient;
import com.hospital.management.exceptions.DuplicateResourceException;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.PatientMapper;
import com.hospital.management.repositories.HospitalRepository;
import com.hospital.management.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements IPatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final HospitalRepository hospitalRepository;

    @Override
    public PatientDTO createPatient(PatientDTO dto) {
        if (patientRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
        }

        Patient patient = patientMapper.toEntity(dto);
        
        // Assign hospital if hospitalId is provided (Phase 10.2)
        if (dto.getHospitalId() != null) {
            Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Hospital not found with id: " + dto.getHospitalId()));
            patient.setHospital(hospital);
        }
        
        Patient saved = patientRepository.save(patient);
        return patientMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return patientMapper.toDTO(patient);
    }

    @Override
    public PatientDTO updatePatient(Long id, PatientDTO dto) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        // Update fields
        patient.setFirstName(dto.getFirstName());
        patient.setLastName(dto.getLastName());
        patient.setEmail(dto.getEmail());
        patient.setPhone(dto.getPhone());
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setGender(dto.getGender());
        patient.setBloodType(dto.getBloodType());
        patient.setAddress(dto.getAddress());
        patient.setEmergencyContact(dto.getEmergencyContact());
        patient.setInsuranceNumber(dto.getInsuranceNumber());

        // Update hospital if hospitalId is provided (Phase 10.2)
        if (dto.getHospitalId() != null) {
            Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Hospital not found with id: " + dto.getHospitalId()));
            patient.setHospital(hospital);
        }

        Patient updated = patientRepository.save(patient);
        return patientMapper.toDTO(updated);
    }

    @Override
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        patientRepository.delete(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(patientMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Hospital-scoped queries (Phase 10.3)
    @Override
    @Transactional(readOnly = true)
    public List<PatientDTO> getPatientsByHospital(Long hospitalId) {
        // Validate hospital exists
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital not found with id: " + hospitalId);
        }
        
        return patientRepository.findByHospitalId(hospitalId).stream()
                .map(patientMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countPatientsByHospital(Long hospitalId) {
        // Validate hospital exists
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital not found with id: " + hospitalId);
        }
        
        return patientRepository.countByHospitalId(hospitalId);
    }
}
