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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceImplTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private PrescriptionMapper prescriptionMapper;

    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    private Patient patient;
    private Doctor doctor;
    private MedicalRecord medicalRecord;
    private Prescription prescription;
    private PrescriptionDTO prescriptionDTO;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);

        doctor = new Doctor();
        doctor.setId(1L);

        medicalRecord = new MedicalRecord();
        medicalRecord.setId(1L);

        prescription = new Prescription();
        prescription.setId(1L);
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setMedicalRecord(medicalRecord);
        prescription.setPrescribedDate(LocalDateTime.now());
        prescription.setValidUntil(LocalDate.now().plusDays(30));
        prescription.setStatus(PrescriptionStatus.ACTIVE);
        prescription.setMedicationName("Amoxicillin");
        prescription.setDosage("500mg");
        prescription.setFrequency("Three times daily");

        prescriptionDTO = new PrescriptionDTO();
        prescriptionDTO.setId(1L);
        prescriptionDTO.setPatientId(1L);
        prescriptionDTO.setDoctorId(1L);
        prescriptionDTO.setMedicalRecordId(1L);
        prescriptionDTO.setPrescribedDate(LocalDateTime.now());
        prescriptionDTO.setValidUntil(LocalDate.now().plusDays(30));
        prescriptionDTO.setStatus(PrescriptionStatus.ACTIVE);
        prescriptionDTO.setMedicationName("Amoxicillin");
        prescriptionDTO.setDosage("500mg");
        prescriptionDTO.setFrequency("Three times daily");
    }

    @Test
    void createPrescription_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(medicalRecord));
        when(prescriptionMapper.toEntity(prescriptionDTO)).thenReturn(prescription);
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(prescription);
        when(prescriptionMapper.toDTO(prescription)).thenReturn(prescriptionDTO);

        PrescriptionDTO result = prescriptionService.createPrescription(prescriptionDTO);

        assertNotNull(result);
        assertEquals(prescriptionDTO.getId(), result.getId());
        verify(prescriptionRepository).save(any(Prescription.class));
    }

    @Test
    void createPrescription_PatientNotFound_ThrowsException() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            prescriptionService.createPrescription(prescriptionDTO);
        });

        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    void createPrescription_DoctorNotFound_ThrowsException() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            prescriptionService.createPrescription(prescriptionDTO);
        });

        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    void createPrescription_MedicalRecordNotFound_ThrowsException() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            prescriptionService.createPrescription(prescriptionDTO);
        });

        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    void createPrescription_WithoutMedicalRecord_Success() {
        prescriptionDTO.setMedicalRecordId(null);
        prescription.setMedicalRecord(null);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(prescriptionMapper.toEntity(prescriptionDTO)).thenReturn(prescription);
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(prescription);
        when(prescriptionMapper.toDTO(prescription)).thenReturn(prescriptionDTO);

        PrescriptionDTO result = prescriptionService.createPrescription(prescriptionDTO);

        assertNotNull(result);
        verify(medicalRecordRepository, never()).findById(any());
        verify(prescriptionRepository).save(any(Prescription.class));
    }

    @Test
    void getPrescriptionById_Success() {
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
        when(prescriptionMapper.toDTO(prescription)).thenReturn(prescriptionDTO);

        PrescriptionDTO result = prescriptionService.getPrescriptionById(1L);

        assertNotNull(result);
        assertEquals(prescriptionDTO.getId(), result.getId());
    }

    @Test
    void getPrescriptionById_NotFound_ThrowsException() {
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            prescriptionService.getPrescriptionById(1L);
        });
    }

    @Test
    void updatePrescription_Success() {
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
        when(prescriptionRepository.save(prescription)).thenReturn(prescription);
        when(prescriptionMapper.toDTO(prescription)).thenReturn(prescriptionDTO);

        PrescriptionDTO result = prescriptionService.updatePrescription(1L, prescriptionDTO);

        assertNotNull(result);
        verify(prescriptionRepository).save(prescription);
    }

    @Test
    void updatePrescriptionStatus_Success() {
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
        when(prescriptionRepository.save(prescription)).thenReturn(prescription);
        when(prescriptionMapper.toDTO(prescription)).thenReturn(prescriptionDTO);

        PrescriptionDTO result = prescriptionService.updatePrescriptionStatus(1L, PrescriptionStatus.DISPENSED);

        assertNotNull(result);
        verify(prescriptionRepository).save(prescription);
    }

    @Test
    void deletePrescription_Success() {
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));

        prescriptionService.deletePrescription(1L);

        verify(prescriptionRepository).delete(prescription);
    }

    @Test
    void getPatientPrescriptions_Success() {
        List<Prescription> prescriptions = Arrays.asList(prescription);
        when(prescriptionRepository.findByPatientIdOrderByPrescribedDateDesc(1L)).thenReturn(prescriptions);
        when(prescriptionMapper.toDTO(prescription)).thenReturn(prescriptionDTO);

        List<PrescriptionDTO> result = prescriptionService.getPatientPrescriptions(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getDoctorPrescriptions_Success() {
        List<Prescription> prescriptions = Arrays.asList(prescription);
        when(prescriptionRepository.findByDoctorIdOrderByPrescribedDateDesc(1L)).thenReturn(prescriptions);
        when(prescriptionMapper.toDTO(prescription)).thenReturn(prescriptionDTO);

        List<PrescriptionDTO> result = prescriptionService.getDoctorPrescriptions(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getPatientActivePrescriptions_Success() {
        List<Prescription> prescriptions = Arrays.asList(prescription);
        when(prescriptionRepository.findByPatientIdAndStatusOrderByPrescribedDateDesc(1L, PrescriptionStatus.ACTIVE))
                .thenReturn(prescriptions);
        when(prescriptionMapper.toDTO(prescription)).thenReturn(prescriptionDTO);

        List<PrescriptionDTO> result = prescriptionService.getPatientActivePrescriptions(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getMedicalRecordPrescriptions_Success() {
        List<Prescription> prescriptions = Arrays.asList(prescription);
        when(prescriptionRepository.findByMedicalRecordIdOrderByPrescribedDateDesc(1L)).thenReturn(prescriptions);
        when(prescriptionMapper.toDTO(prescription)).thenReturn(prescriptionDTO);

        List<PrescriptionDTO> result = prescriptionService.getMedicalRecordPrescriptions(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
