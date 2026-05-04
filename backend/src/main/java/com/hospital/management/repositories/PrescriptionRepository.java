package com.hospital.management.repositories;

import com.hospital.management.entities.Prescription;
import com.hospital.management.enums.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByPatientIdOrderByPrescribedDateDesc(Long patientId);

    List<Prescription> findByDoctorIdOrderByPrescribedDateDesc(Long doctorId);

    List<Prescription> findByPatientIdAndStatusOrderByPrescribedDateDesc(Long patientId, PrescriptionStatus status);

    List<Prescription> findByMedicalRecordIdOrderByPrescribedDateDesc(Long medicalRecordId);

    Long countByStatus(PrescriptionStatus status);

    Long countByDoctorId(Long doctorId);

    // Phase 10.6: Hospital-scoped queries
    List<Prescription> findByHospitalIdOrderByPrescribedDateDesc(Long hospitalId);

    @Query("SELECT p FROM Prescription p WHERE p.doctor.id = :doctorId AND p.hospital.id = :hospitalId ORDER BY p.prescribedDate DESC")
    List<Prescription> findByDoctorIdAndHospitalId(@Param("doctorId") Long doctorId, @Param("hospitalId") Long hospitalId);

    Long countByHospitalId(Long hospitalId);

    Long countByHospitalIdAndStatus(Long hospitalId, PrescriptionStatus status);
}
