package com.hospital.management.repositories;

import com.hospital.management.entities.HospitalDirector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalDirectorRepository extends JpaRepository<HospitalDirector, Long> {
    Optional<HospitalDirector> findByEmail(String email);
    boolean existsByEmail(String email);

    // Hospital-scoped queries (Phase 10.3)
    List<HospitalDirector> findByHospitalId(Long hospitalId);

    Long countByHospitalId(Long hospitalId);

    boolean existsByHospitalId(Long hospitalId);
}
