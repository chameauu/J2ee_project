package com.hospital.management.services;

import com.hospital.management.dto.PharmacistDTO;

import java.util.List;

public interface IPharmacistService {
    PharmacistDTO createPharmacist(PharmacistDTO dto);
    PharmacistDTO getPharmacistById(Long id);
    PharmacistDTO updatePharmacist(Long id, PharmacistDTO dto);
    void deletePharmacist(Long id);
    List<PharmacistDTO> getAllPharmacists();
}
