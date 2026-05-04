package com.hospital.management.services;

import com.hospital.management.dto.MedicationDTO;
import com.hospital.management.enums.MedicationType;

import java.util.List;

public interface IMedicationService {
    MedicationDTO createMedication(MedicationDTO dto);
    MedicationDTO getMedicationById(Long id);
    List<MedicationDTO> getAllMedications();
    MedicationDTO updateMedication(Long id, MedicationDTO dto);
    void deleteMedication(Long id);
    List<MedicationDTO> searchMedications(String keyword);
    List<MedicationDTO> getMedicationsByType(MedicationType type);
    List<MedicationDTO> getMedicationsByManufacturer(String manufacturer);
}
