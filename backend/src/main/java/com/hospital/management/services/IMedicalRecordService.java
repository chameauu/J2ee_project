package com.hospital.management.services;

import com.hospital.management.dto.MedicalRecordDTO;

import java.util.List;

public interface IMedicalRecordService {

    MedicalRecordDTO createMedicalRecord(MedicalRecordDTO dto);

    MedicalRecordDTO getMedicalRecordById(Long id);

    MedicalRecordDTO updateMedicalRecord(Long id, MedicalRecordDTO dto);

    List<MedicalRecordDTO> getPatientMedicalHistory(Long patientId);

    List<MedicalRecordDTO> getDoctorMedicalRecords(Long doctorId);
}
