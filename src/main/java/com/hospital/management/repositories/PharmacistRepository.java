package com.hospital.management.repositories;

import com.hospital.management.entities.Pharmacist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacistRepository extends JpaRepository<Pharmacist, Long> {
    Optional<Pharmacist> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByLicenseNumber(String licenseNumber);

    // Hospital-scoped queries (Phase 10.3)
    List<Pharmacist> findByHospitalId(Long hospitalId);

    Long countByHospitalId(Long hospitalId);

    boolean existsByHospitalId(Long hospitalId);
}
