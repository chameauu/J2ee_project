package com.hospital.management.services;

import com.hospital.management.dto.PrescriptionDTO;
import com.hospital.management.enums.PrescriptionStatus;

import java.util.List;

public interface IPrescriptionService {

    PrescriptionDTO createPrescription(PrescriptionDTO dto);

    PrescriptionDTO getPrescriptionById(Long id);

    PrescriptionDTO updatePrescription(Long id, PrescriptionDTO dto);

    PrescriptionDTO updatePrescriptionStatus(Long id, PrescriptionStatus status);

    void deletePrescription(Long id);

    List<PrescriptionDTO> getPatientPrescriptions(Long patientId);

    List<PrescriptionDTO> getDoctorPrescriptions(Long doctorId);

    List<PrescriptionDTO> getPatientActivePrescriptions(Long patientId);

    List<PrescriptionDTO> getMedicalRecordPrescriptions(Long medicalRecordId);
}
