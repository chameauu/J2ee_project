package com.hospital.management.repositories;

import com.hospital.management.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByEmail(String email);

    boolean existsByEmail(String email);

    // Hospital-scoped queries (Phase 10.3)
    List<Patient> findByHospitalId(Long hospitalId);

    Long countByHospitalId(Long hospitalId);

    boolean existsByHospitalId(Long hospitalId);
}
