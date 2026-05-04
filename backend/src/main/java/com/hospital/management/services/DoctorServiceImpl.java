package com.hospital.management.services;

import com.hospital.management.dto.DoctorDTO;
import com.hospital.management.entities.Doctor;
import com.hospital.management.entities.Hospital;
import com.hospital.management.exceptions.DuplicateResourceException;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.DoctorMapper;
import com.hospital.management.repositories.DoctorRepository;
import com.hospital.management.repositories.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorServiceImpl implements IDoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;
    private final HospitalRepository hospitalRepository;

    @Override
    public DoctorDTO createDoctor(DoctorDTO dto) {
        if (doctorRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
        }
        if (doctorRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new DuplicateResourceException("License number already exists: " + dto.getLicenseNumber());
        }

        Doctor doctor = doctorMapper.toEntity(dto);
        
        // Assign hospital if hospitalId is provided (Phase 10.2)
        if (dto.getHospitalId() != null) {
            Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Hospital not found with id: " + dto.getHospitalId()));
            doctor.setHospital(hospital);
        }
        
        Doctor saved = doctorRepository.save(doctor);
        return doctorMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        return doctorMapper.toDTO(doctor);
    }

    @Override
    public DoctorDTO updateDoctor(Long id, DoctorDTO dto) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        doctor.setFirstName(dto.getFirstName());
        doctor.setLastName(dto.getLastName());
        doctor.setEmail(dto.getEmail());
        doctor.setPhone(dto.getPhone());
        doctor.setSpecialization(dto.getSpecialization());
        doctor.setLicenseNumber(dto.getLicenseNumber());
        doctor.setYearsOfExperience(dto.getYearsOfExperience());
        doctor.setQualification(dto.getQualification());

        // Update hospital if hospitalId is provided (Phase 10.2)
        if (dto.getHospitalId() != null) {
            Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Hospital not found with id: " + dto.getHospitalId()));
            doctor.setHospital(hospital);
        }

        Doctor updated = doctorRepository.save(doctor);
        return doctorMapper.toDTO(updated);
    }

    @Override
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        doctorRepository.delete(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDTO> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDTO> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization).stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Hospital-scoped queries (Phase 10.3)
    @Override
    @Transactional(readOnly = true)
    public List<DoctorDTO> getDoctorsByHospital(Long hospitalId) {
        // Validate hospital exists
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital not found with id: " + hospitalId);
        }
        
        return doctorRepository.findByHospitalId(hospitalId).stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDTO> getDoctorsByHospitalAndSpecialization(Long hospitalId, String specialization) {
        // Validate hospital exists
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital not found with id: " + hospitalId);
        }
        
        return doctorRepository.findByHospitalIdAndSpecialization(hospitalId, specialization).stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countDoctorsByHospital(Long hospitalId) {
        // Validate hospital exists
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital not found with id: " + hospitalId);
        }
        
        return doctorRepository.countByHospitalId(hospitalId);
    }
}
