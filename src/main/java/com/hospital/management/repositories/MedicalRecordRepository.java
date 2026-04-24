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
}
