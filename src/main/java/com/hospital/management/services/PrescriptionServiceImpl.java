package com.hospital.management.services;

import com.hospital.management.dto.PrescriptionDTO;
import com.hospital.management.entities.Doctor;
import com.hospital.management.entities.MedicalRecord;
import com.hospital.management.entities.Patient;
import com.hospital.management.entities.Prescription;
import com.hospital.management.enums.PrescriptionStatus;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.PrescriptionMapper;
import com.hospital.management.repositories.DoctorRepository;
import com.hospital.management.repositories.MedicalRecordRepository;
import com.hospital.management.repositories.PatientRepository;
import com.hospital.management.repositories.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PrescriptionServiceImpl implements IPrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionMapper prescriptionMapper;

    @Override
    public PrescriptionDTO createPrescription(PrescriptionDTO dto) {
        // Validate patient exists
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + dto.getPatientId()));

        // Validate doctor exists
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + dto.getDoctorId()));

        // Validate medical record if provided
        MedicalRecord medicalRecord = null;
        if (dto.getMedicalRecordId() != null) {
            medicalRecord = medicalRecordRepository.findById(dto.getMedicalRecordId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + dto.getMedicalRecordId()));
        }

        // Convert DTO to entity
        Prescription prescription = prescriptionMapper.toEntity(dto);
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setMedicalRecord(medicalRecord);

        // Save
        Prescription saved = prescriptionRepository.save(prescription);

        // Convert back to DTO
        return prescriptionMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionDTO getPrescriptionById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id));
        return prescriptionMapper.toDTO(prescription);
    }

    @Override
    public PrescriptionDTO updatePrescription(Long id, PrescriptionDTO dto) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id));

        // Update fields
        prescription.setMedicationName(dto.getMedicationName());
        prescription.setDosage(dto.getDosage());
        prescription.setFrequency(dto.getFrequency());
        prescription.setDurationDays(dto.getDurationDays());
        prescription.setInstructions(dto.getInstructions());
        prescription.setNotes(dto.getNotes());
        prescription.setValidUntil(dto.getValidUntil());

        Prescription updated = prescriptionRepository.save(prescription);
        return prescriptionMapper.toDTO(updated);
    }

    @Override
    public PrescriptionDTO updatePrescriptionStatus(Long id, PrescriptionStatus status) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id));

        prescription.setStatus(status);
        Prescription updated = prescriptionRepository.save(prescription);

        return prescriptionMapper.toDTO(updated);
    }

    @Override
    public void deletePrescription(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id));
        prescriptionRepository.delete(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getPatientPrescriptions(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByPrescribedDateDesc(patientId)
                .stream()
                .map(prescriptionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getDoctorPrescriptions(Long doctorId) {
        return prescriptionRepository.findByDoctorIdOrderByPrescribedDateDesc(doctorId)
                .stream()
                .map(prescriptionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getPatientActivePrescriptions(Long patientId) {
        return prescriptionRepository.findByPatientIdAndStatusOrderByPrescribedDateDesc(patientId, PrescriptionStatus.ACTIVE)
                .stream()
                .map(prescriptionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getMedicalRecordPrescriptions(Long medicalRecordId) {
        return prescriptionRepository.findByMedicalRecordIdOrderByPrescribedDateDesc(medicalRecordId)
                .stream()
                .map(prescriptionMapper::toDTO)
                .collect(Collectors.toList());
    }
}
