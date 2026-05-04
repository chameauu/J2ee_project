package com.hospital.management.services;

import com.hospital.management.dto.AppointmentDTO;
import com.hospital.management.entities.Appointment;
import com.hospital.management.entities.Doctor;
import com.hospital.management.entities.Patient;
import com.hospital.management.enums.AppointmentStatus;
import com.hospital.management.enums.AppointmentType;
import com.hospital.management.exceptions.BadRequestException;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.AppointmentMapper;
import com.hospital.management.repositories.AppointmentRepository;
import com.hospital.management.repositories.DoctorRepository;
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
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Test
    void shouldCreateAppointment() {
        // Given
        AppointmentDTO dto = new AppointmentDTO();
        dto.setPatientId(1L);
        dto.setDoctorId(2L);
        dto.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        dto.setDurationMinutes(30);
        dto.setStatus(AppointmentStatus.SCHEDULED);
        dto.setType(AppointmentType.CONSULTATION);

        Patient patient = new Patient();
        patient.setId(1L);

        Doctor doctor = new Doctor();
        doctor.setId(2L);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);

        Appointment savedAppointment = new Appointment();
        savedAppointment.setId(1L);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.hasTimeSlotConflict(any(), any(), any())).thenReturn(false);
        when(appointmentMapper.toEntity(dto)).thenReturn(appointment);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);
        when(appointmentMapper.toDTO(savedAppointment)).thenReturn(dto);

        // When
        AppointmentDTO result = appointmentService.createAppointment(dto);

        // Then
        assertNotNull(result);
        verify(patientRepository).findById(1L);
        verify(doctorRepository).findById(2L);
        verify(appointmentRepository).hasTimeSlotConflict(any(), any(), any());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void shouldThrowExceptionWhenPatientNotFound() {
        // Given
        AppointmentDTO dto = new AppointmentDTO();
        dto.setPatientId(999L);
        dto.setDoctorId(2L);

        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.createAppointment(dto));
    }

    @Test
    void shouldThrowExceptionWhenDoctorNotFound() {
        // Given
        AppointmentDTO dto = new AppointmentDTO();
        dto.setPatientId(1L);
        dto.setDoctorId(999L);

        Patient patient = new Patient();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.createAppointment(dto));
    }

    @Test
    void shouldThrowExceptionWhenTimeSlotConflict() {
        // Given
        AppointmentDTO dto = new AppointmentDTO();
        dto.setPatientId(1L);
        dto.setDoctorId(2L);
        dto.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        dto.setDurationMinutes(30);

        Patient patient = new Patient();
        Doctor doctor = new Doctor();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.hasTimeSlotConflict(any(), any(), any())).thenReturn(true);

        // When & Then
        assertThrows(BadRequestException.class,
                () -> appointmentService.createAppointment(dto));
    }

    @Test
    void shouldGetAppointmentById() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setId(1L);

        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.toDTO(appointment)).thenReturn(dto);

        // When
        AppointmentDTO result = appointmentService.getAppointmentById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(appointmentRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenAppointmentNotFound() {
        // Given
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.getAppointmentById(999L));
    }

    @Test
    void shouldUpdateAppointmentStatus() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(1L);
        dto.setStatus(AppointmentStatus.COMPLETED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toDTO(appointment)).thenReturn(dto);

        // When
        AppointmentDTO result = appointmentService.updateAppointmentStatus(1L, AppointmentStatus.COMPLETED);

        // Then
        assertNotNull(result);
        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository).save(appointment);
        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
    }

    @Test
    void shouldGetPatientAppointments() {
        // Given
        Appointment appointment1 = new Appointment();
        appointment1.setId(1L);
        Appointment appointment2 = new Appointment();
        appointment2.setId(2L);

        List<Appointment> appointments = Arrays.asList(appointment1, appointment2);

        AppointmentDTO dto1 = new AppointmentDTO();
        dto1.setId(1L);
        AppointmentDTO dto2 = new AppointmentDTO();
        dto2.setId(2L);

        when(appointmentRepository.findByPatientIdOrderByAppointmentDateTimeDesc(1L))
                .thenReturn(appointments);
        when(appointmentMapper.toDTO(appointment1)).thenReturn(dto1);
        when(appointmentMapper.toDTO(appointment2)).thenReturn(dto2);

        // When
        List<AppointmentDTO> result = appointmentService.getPatientAppointments(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(appointmentRepository).findByPatientIdOrderByAppointmentDateTimeDesc(1L);
    }

    @Test
    void shouldGetDoctorAppointments() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setId(1L);

        List<Appointment> appointments = Arrays.asList(appointment);

        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(1L);

        when(appointmentRepository.findByDoctorIdOrderByAppointmentDateTimeAsc(2L))
                .thenReturn(appointments);
        when(appointmentMapper.toDTO(appointment)).thenReturn(dto);

        // When
        List<AppointmentDTO> result = appointmentService.getDoctorAppointments(2L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appointmentRepository).findByDoctorIdOrderByAppointmentDateTimeAsc(2L);
    }

    @Test
    void shouldCancelAppointment() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(1L);
        dto.setStatus(AppointmentStatus.CANCELLED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toDTO(appointment)).thenReturn(dto);

        // When
        AppointmentDTO result = appointmentService.cancelAppointment(1L);

        // Then
        assertNotNull(result);
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        verify(appointmentRepository).save(appointment);
    }
}
