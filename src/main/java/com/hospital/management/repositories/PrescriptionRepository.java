package com.hospital.management.repositories;

import com.hospital.management.entities.Prescription;
import com.hospital.management.enums.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
