package com.hospital.management.services;

import com.hospital.management.dto.MedicalRecordDTO;
import com.hospital.management.entities.Doctor;
import com.hospital.management.entities.MedicalRecord;
import com.hospital.management.entities.Patient;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.MedicalRecordMapper;
import com.hospital.management.repositories.DoctorRepository;
import com.hospital.management.repositories.MedicalRecordRepository;
import com.hospital.management.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicalRecordServiceImpl implements IMedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalRecordMapper medicalRecordMapper;

    @Override
    public MedicalRecordDTO createMedicalRecord(MedicalRecordDTO dto) {
        // Validate patient exists
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + dto.getPatientId()));

        // Validate doctor exists
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + dto.getDoctorId()));

        // Convert DTO to entity
        MedicalRecord medicalRecord = medicalRecordMapper.toEntity(dto);
        medicalRecord.setPatient(patient);
        medicalRecord.setDoctor(doctor);
        medicalRecord.setHospital(doctor.getHospital()); // Phase 10.6: Set hospital from doctor
        medicalRecord.setVisitDate(LocalDateTime.now());

        // Save
        MedicalRecord saved = medicalRecordRepository.save(medicalRecord);

        // Convert back to DTO
        return medicalRecordMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordDTO getMedicalRecordById(Long id) {
        MedicalRecord medicalRecord = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + id));
        return medicalRecordMapper.toDTO(medicalRecord);
    }

    @Override
    public MedicalRecordDTO updateMedicalRecord(Long id, MedicalRecordDTO dto) {
        // Find existing record
        MedicalRecord medicalRecord = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + id));

        // Validate patient exists
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + dto.getPatientId()));

        // Validate doctor exists
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + dto.getDoctorId()));

        // Update fields
        medicalRecord.setPatient(patient);
        medicalRecord.setDoctor(doctor);
        medicalRecord.setHospital(doctor.getHospital()); // Phase 10.6: Update hospital from doctor
        medicalRecord.setChiefComplaint(dto.getChiefComplaint());
        medicalRecord.setDiagnosis(dto.getDiagnosis());
        medicalRecord.setTreatment(dto.getTreatment());
        medicalRecord.setNotes(dto.getNotes());
        medicalRecord.setVitalSigns(dto.getVitalSigns());

        // Save
        MedicalRecord updated = medicalRecordRepository.save(medicalRecord);

        // Convert back to DTO
        return medicalRecordMapper.toDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordDTO> getPatientMedicalHistory(Long patientId) {
        return medicalRecordRepository.findByPatientIdOrderByVisitDateDesc(patientId)
                .stream()
                .map(medicalRecordMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordDTO> getDoctorMedicalRecords(Long doctorId) {
        return medicalRecordRepository.findByDoctorIdOrderByVisitDateDesc(doctorId)
                .stream()
                .map(medicalRecordMapper::toDTO)
                .collect(Collectors.toList());
    }
}
