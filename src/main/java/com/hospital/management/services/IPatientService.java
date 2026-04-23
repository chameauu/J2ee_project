package com.hospital.management.services;

import com.hospital.management.dto.PatientDTO;

public interface IPatientService {

    PatientDTO createPatient(PatientDTO dto);

    PatientDTO getPatientById(Long id);
}
