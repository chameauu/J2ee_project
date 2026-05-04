package com.hospital.management.services;

import com.hospital.management.dto.DoctorDTO;

import java.util.List;

public interface IDoctorService {
    DoctorDTO createDoctor(DoctorDTO dto);
    DoctorDTO getDoctorById(Long id);
    DoctorDTO updateDoctor(Long id, DoctorDTO dto);
    void deleteDoctor(Long id);
    List<DoctorDTO> getAllDoctors();
    List<DoctorDTO> getDoctorsBySpecialization(String specialization);

    // Hospital-scoped queries (Phase 10.3)
    List<DoctorDTO> getDoctorsByHospital(Long hospitalId);
    List<DoctorDTO> getDoctorsByHospitalAndSpecialization(Long hospitalId, String specialization);
    Long countDoctorsByHospital(Long hospitalId);
}
