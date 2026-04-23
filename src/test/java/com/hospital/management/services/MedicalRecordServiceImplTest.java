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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceImplTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private MedicalRecordMapper medicalRecordMapper;

    @InjectMocks
    private MedicalRecordServiceImpl medicalRecordService;

    @Test
    void shouldCreateMedicalRecord() {
        // Given
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setPatientId(1L);
        dto.setDoctorId(2L);
        dto.setChiefComplaint("Headache");
        dto.setDiagnosis("Migraine");
        dto.setTreatment("Pain relievers");

        Patient patient = new Patient();
        patient.setId(1L);

        Doctor doctor = new Doctor();
        doctor.setId(2L);

        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setPatient(patient);
        medicalRecord.setDoctor(doctor);
        medicalRecord.setChiefComplaint("Headache");

        MedicalRecord savedRecord = new MedicalRecord();
        savedRecord.setId(1L);
        savedRecord.setPatient(patient);
        savedRecord.setDoctor(doctor);
        savedRecord.setVisitDate(LocalDateTime.now());

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(medicalRecordMapper.toEntity(dto)).thenReturn(medicalRecord);
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(savedRecord);
        when(medicalRecordMapper.toDTO(savedRecord)).thenReturn(dto);

        // When
        MedicalRecordDTO result = medicalRecordService.createMedicalRecord(dto);

        // Then
        assertNotNull(result);
        verify(patientRepository).findById(1L);
        verify(doctorRepository).findById(2L);
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
    }

    @Test
    void shouldThrowExceptionWhenPatientNotFound() {
        // Given
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setPatientId(999L);
        dto.setDoctorId(2L);

        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> medicalRecordService.createMedicalRecord(dto));
    }

    @Test
    void shouldThrowExceptionWhenDoctorNotFound() {
        // Given
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setPatientId(1L);
        dto.setDoctorId(999L);

        Patient patient = new Patient();
        patient.setId(1L);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> medicalRecordService.createMedicalRecord(dto));
    }

    @Test
    void shouldGetMedicalRecordById() {
        // Given
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setId(1L);

        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setId(1L);

        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(medicalRecord));
        when(medicalRecordMapper.toDTO(medicalRecord)).thenReturn(dto);

        // When
        MedicalRecordDTO result = medicalRecordService.getMedicalRecordById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(medicalRecordRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenMedicalRecordNotFound() {
        // Given
        when(medicalRecordRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> medicalRecordService.getMedicalRecordById(999L));
    }

    @Test
    void shouldUpdateMedicalRecord() {
        // Given
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setPatientId(1L);
        dto.setDoctorId(2L);
        dto.setChiefComplaint("Updated complaint");
        dto.setDiagnosis("Updated diagnosis");
        dto.setTreatment("Updated treatment");
        dto.setNotes("Updated notes");

        Patient patient = new Patient();
        patient.setId(1L);

        Doctor doctor = new Doctor();
        doctor.setId(2L);

        MedicalRecord existingRecord = new MedicalRecord();
        existingRecord.setId(1L);
        existingRecord.setPatient(patient);
        existingRecord.setDoctor(doctor);

        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(existingRecord));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(existingRecord);
        when(medicalRecordMapper.toDTO(existingRecord)).thenReturn(dto);

        // When
        MedicalRecordDTO result = medicalRecordService.updateMedicalRecord(1L, dto);

        // Then
        assertNotNull(result);
        verify(medicalRecordRepository).findById(1L);
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentMedicalRecord() {
        // Given
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setPatientId(1L);
        dto.setDoctorId(2L);

        when(medicalRecordRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> medicalRecordService.updateMedicalRecord(999L, dto));
    }

    @Test
    void shouldGetPatientMedicalHistory() {
        // Given
        MedicalRecord record1 = new MedicalRecord();
        record1.setId(1L);
        MedicalRecord record2 = new MedicalRecord();
        record2.setId(2L);

        List<MedicalRecord> records = Arrays.asList(record1, record2);

        MedicalRecordDTO dto1 = new MedicalRecordDTO();
        dto1.setId(1L);
        MedicalRecordDTO dto2 = new MedicalRecordDTO();
        dto2.setId(2L);

        when(medicalRecordRepository.findByPatientIdOrderByVisitDateDesc(1L)).thenReturn(records);
        when(medicalRecordMapper.toDTO(record1)).thenReturn(dto1);
        when(medicalRecordMapper.toDTO(record2)).thenReturn(dto2);

        // When
        List<MedicalRecordDTO> result = medicalRecordService.getPatientMedicalHistory(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(medicalRecordRepository).findByPatientIdOrderByVisitDateDesc(1L);
    }

    @Test
    void shouldGetDoctorMedicalRecords() {
        // Given
        MedicalRecord record1 = new MedicalRecord();
        record1.setId(1L);

        List<MedicalRecord> records = Arrays.asList(record1);

        MedicalRecordDTO dto1 = new MedicalRecordDTO();
        dto1.setId(1L);

        when(medicalRecordRepository.findByDoctorIdOrderByVisitDateDesc(2L)).thenReturn(records);
        when(medicalRecordMapper.toDTO(record1)).thenReturn(dto1);

        // When
        List<MedicalRecordDTO> result = medicalRecordService.getDoctorMedicalRecords(2L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(medicalRecordRepository).findByDoctorIdOrderByVisitDateDesc(2L);
    }
}
