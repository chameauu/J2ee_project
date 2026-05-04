package com.hospital.management.repositories;

import com.hospital.management.entities.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByLicenseNumber(String licenseNumber);
    List<Doctor> findBySpecialization(String specialization);

    // Hospital-scoped queries (Phase 10.3)
    List<Doctor> findByHospitalId(Long hospitalId);

    List<Doctor> findByHospitalIdAndSpecialization(Long hospitalId, String specialization);

    Long countByHospitalId(Long hospitalId);

    boolean existsByHospitalId(Long hospitalId);
}
