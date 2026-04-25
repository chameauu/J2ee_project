package com.hospital.management.repositories;

import com.hospital.management.entities.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {
    
    List<PrescriptionItem> findByPrescriptionId(Long prescriptionId);
    
    List<PrescriptionItem> findByMedicationId(Long medicationId);
    
    @Query("SELECT pi FROM PrescriptionItem pi WHERE pi.prescription.id = :prescriptionId AND pi.dispensed = false")
    List<PrescriptionItem> findUndispensedByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
    
    @Query("SELECT pi FROM PrescriptionItem pi WHERE pi.dispensed = true AND pi.dispensedBy.id = :pharmacistId")
    List<PrescriptionItem> findDispensedByPharmacistId(@Param("pharmacistId") Long pharmacistId);
    
    @Query("SELECT COUNT(pi) FROM PrescriptionItem pi WHERE pi.prescription.id = :prescriptionId AND pi.dispensed = false")
    Long countUndispensedByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
}
