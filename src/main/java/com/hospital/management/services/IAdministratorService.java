package com.hospital.management.services;

import com.hospital.management.dto.AdministratorDTO;

import java.util.List;

public interface IAdministratorService {

    AdministratorDTO createAdministrator(AdministratorDTO dto);

    AdministratorDTO getAdministratorById(Long id);

    AdministratorDTO updateAdministrator(Long id, AdministratorDTO dto);

    void deleteAdministrator(Long id);

    List<AdministratorDTO> getAllAdministrators();
}
