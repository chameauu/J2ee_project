package com.hospital.management.services;

import com.hospital.management.dto.PharmacyStockDTO;

import java.util.List;

public interface IPharmacyStockService {
    PharmacyStockDTO createStock(PharmacyStockDTO dto);
    PharmacyStockDTO getStockById(Long id);
    List<PharmacyStockDTO> getAllStock();
    PharmacyStockDTO updateStock(Long id, PharmacyStockDTO dto);
    void deleteStock(Long id);
    List<PharmacyStockDTO> getStockByMedicationId(Long medicationId);
    List<PharmacyStockDTO> getLowStockItems();
    List<PharmacyStockDTO> getExpiringSoonItems();
    List<PharmacyStockDTO> getExpiredStock();
    PharmacyStockDTO reduceStock(Long id, Integer amount);
    PharmacyStockDTO addStock(Long id, Integer amount);
}
