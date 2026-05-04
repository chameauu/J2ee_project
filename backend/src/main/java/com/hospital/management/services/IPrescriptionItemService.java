package com.hospital.management.services;

import com.hospital.management.dto.PrescriptionItemDTO;

import java.util.List;

public interface IPrescriptionItemService {
    
    PrescriptionItemDTO createPrescriptionItem(PrescriptionItemDTO dto);
    
    PrescriptionItemDTO getPrescriptionItemById(Long id);
    
    List<PrescriptionItemDTO> getItemsByPrescriptionId(Long prescriptionId);
    
    List<PrescriptionItemDTO> getItemsByMedicationId(Long medicationId);
    
    PrescriptionItemDTO updatePrescriptionItem(Long id, PrescriptionItemDTO dto);
    
    void deletePrescriptionItem(Long id);
    
    PrescriptionItemDTO dispenseItem(Long id, Long pharmacistId);
    
    List<PrescriptionItemDTO> getUndispensedItemsByPrescriptionId(Long prescriptionId);
    
    List<PrescriptionItemDTO> getDispensedItemsByPharmacistId(Long pharmacistId);
}
