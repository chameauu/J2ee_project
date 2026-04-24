package com.hospital.management.repositories;

import com.hospital.management.entities.PharmacyStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PharmacyStockRepository extends JpaRepository<PharmacyStock, Long> {
    
    List<PharmacyStock> findByMedicationId(Long medicationId);
    
    @Query("SELECT ps FROM PharmacyStock ps WHERE ps.quantity <= ps.reorderLevel")
    List<PharmacyStock> findLowStockItems();
    
    @Query("SELECT ps FROM PharmacyStock ps WHERE ps.expiryDate <= :date")
    List<PharmacyStock> findExpiringSoon(@Param("date") LocalDate date);
    
    @Query("SELECT ps FROM PharmacyStock ps WHERE ps.expiryDate < :date")
    List<PharmacyStock> findExpiredStock(@Param("date") LocalDate date);
}
