package com.hospital.management.services;

import com.hospital.management.dto.HospitalDTO;

import java.util.List;

public interface IHospitalService {

    HospitalDTO createHospital(HospitalDTO hospitalDTO);

    HospitalDTO getHospitalById(Long id);

    HospitalDTO updateHospital(Long id, HospitalDTO hospitalDTO);

    void deleteHospital(Long id);

    List<HospitalDTO> getAllHospitals();

    List<HospitalDTO> searchHospitals(String keyword);
}
