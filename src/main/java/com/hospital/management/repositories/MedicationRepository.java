package com.hospital.management.repositories;

import com.hospital.management.entities.Medication;
import com.hospital.management.enums.MedicationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {
    
    Optional<Medication> findByName(String name);
    
    List<Medication> findByType(MedicationType type);
    
    @Query("SELECT m FROM Medication m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Medication> searchMedications(@Param("keyword") String keyword);
    
    List<Medication> findByManufacturer(String manufacturer);
}
