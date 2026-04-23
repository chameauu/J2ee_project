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
}
