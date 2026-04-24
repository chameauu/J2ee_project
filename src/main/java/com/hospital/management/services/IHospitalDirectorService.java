package com.hospital.management.services;

import com.hospital.management.dto.HospitalDirectorDTO;

import java.util.List;

public interface IHospitalDirectorService {
    HospitalDirectorDTO createHospitalDirector(HospitalDirectorDTO dto);
    HospitalDirectorDTO getHospitalDirectorById(Long id);
    List<HospitalDirectorDTO> getAllHospitalDirectors();
    HospitalDirectorDTO updateHospitalDirector(Long id, HospitalDirectorDTO dto);
    void deleteHospitalDirector(Long id);
}
