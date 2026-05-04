package com.hospital.management.repositories;

import com.hospital.management.entities.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatientIdOrderByVisitDateDesc(Long patientId);

    List<MedicalRecord> findByDoctorIdOrderByVisitDateDesc(Long doctorId);

    Long countByDoctorId(Long doctorId);

    @Query("SELECT COUNT(DISTINCT mr.patient.id) FROM MedicalRecord mr WHERE mr.doctor.id = :doctorId")
    Long countDistinctPatientsByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT COUNT(DISTINCT mr.patient.id) FROM MedicalRecord mr")
    Long countDistinctPatients();

    // Phase 10.6: Hospital-scoped queries
    List<MedicalRecord> findByHospitalIdOrderByVisitDateDesc(Long hospitalId);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.doctor.id = :doctorId AND mr.hospital.id = :hospitalId ORDER BY mr.visitDate DESC")
    List<MedicalRecord> findByDoctorIdAndHospitalId(@Param("doctorId") Long doctorId, @Param("hospitalId") Long hospitalId);

    Long countByHospitalId(Long hospitalId);
}
