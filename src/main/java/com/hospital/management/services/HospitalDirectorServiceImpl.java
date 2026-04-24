package com.hospital.management.services;

import com.hospital.management.dto.HospitalDirectorDTO;
import com.hospital.management.entities.HospitalDirector;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.HospitalDirectorMapper;
import com.hospital.management.repositories.HospitalDirectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HospitalDirectorServiceImpl implements IHospitalDirectorService {

    private final HospitalDirectorRepository hospitalDirectorRepository;
    private final HospitalDirectorMapper hospitalDirectorMapper;

    @Override
    @Transactional
    public HospitalDirectorDTO createHospitalDirector(HospitalDirectorDTO dto) {
        HospitalDirector hospitalDirector = hospitalDirectorMapper.toEntity(dto);
        HospitalDirector saved = hospitalDirectorRepository.save(hospitalDirector);
        return hospitalDirectorMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public HospitalDirectorDTO getHospitalDirectorById(Long id) {
        HospitalDirector hospitalDirector = hospitalDirectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital Director not found with id: " + id));
        return hospitalDirectorMapper.toDTO(hospitalDirector);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HospitalDirectorDTO> getAllHospitalDirectors() {
        return hospitalDirectorRepository.findAll().stream()
                .map(hospitalDirectorMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HospitalDirectorDTO updateHospitalDirector(Long id, HospitalDirectorDTO dto) {
        HospitalDirector existing = hospitalDirectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital Director not found with id: " + id));

        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());
        existing.setHospitalName(dto.getHospitalName());
        existing.setAppointmentDate(dto.getAppointmentDate());
        existing.setCredentials(dto.getCredentials());
        existing.setActive(dto.getActive());

        HospitalDirector updated = hospitalDirectorRepository.save(existing);
        return hospitalDirectorMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteHospitalDirector(Long id) {
        if (!hospitalDirectorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hospital Director not found with id: " + id);
        }
        hospitalDirectorRepository.deleteById(id);
    }
}
