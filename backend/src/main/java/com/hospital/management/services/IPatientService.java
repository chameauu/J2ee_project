package com.hospital.management.services;

import com.hospital.management.dto.PatientDTO;

import java.util.List;

public interface IPatientService {

    PatientDTO createPatient(PatientDTO dto);

    PatientDTO getPatientById(Long id);

    PatientDTO updatePatient(Long id, PatientDTO dto);

    void deletePatient(Long id);

    List<PatientDTO> getAllPatients();

    // Hospital-scoped queries (Phase 10.3)
    List<PatientDTO> getPatientsByHospital(Long hospitalId);

    Long countPatientsByHospital(Long hospitalId);
}
