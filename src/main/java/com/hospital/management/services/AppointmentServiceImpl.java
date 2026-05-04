package com.hospital.management.services;

import com.hospital.management.dto.AppointmentDTO;
import com.hospital.management.entities.Appointment;
import com.hospital.management.entities.Doctor;
import com.hospital.management.entities.Patient;
import com.hospital.management.enums.AppointmentStatus;
import com.hospital.management.exceptions.BadRequestException;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.AppointmentMapper;
import com.hospital.management.repositories.AppointmentRepository;
import com.hospital.management.repositories.DoctorRepository;
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
public class AppointmentServiceImpl implements IAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    public AppointmentDTO createAppointment(AppointmentDTO dto) {
        // Validate patient exists
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + dto.getPatientId()));

        // Validate doctor exists
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + dto.getDoctorId()));

        // Check for time slot conflicts
        LocalDateTime startTime = dto.getAppointmentDateTime();
        LocalDateTime endTime = startTime.plusMinutes(dto.getDurationMinutes());

        boolean hasConflict = appointmentRepository.hasTimeSlotConflict(
                dto.getDoctorId(),
                startTime,
                endTime
        );

        if (hasConflict) {
            throw new BadRequestException("Doctor already has an appointment during this time slot");
        }

        // Convert DTO to entity
        Appointment appointment = appointmentMapper.toEntity(dto);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setHospital(doctor.getHospital()); // Phase 10.6: Set hospital from doctor

        // Save
        Appointment saved = appointmentRepository.save(appointment);

        // Convert back to DTO
        return appointmentMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentDTO getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        return appointmentMapper.toDTO(appointment);
    }

    @Override
    public AppointmentDTO updateAppointmentStatus(Long id, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        appointment.setStatus(status);
        Appointment updated = appointmentRepository.save(appointment);

        return appointmentMapper.toDTO(updated);
    }

    @Override
    public AppointmentDTO cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment updated = appointmentRepository.save(appointment);

        return appointmentMapper.toDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDateTimeDesc(patientId)
                .stream()
                .map(appointmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getDoctorAppointments(Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateTimeAsc(doctorId)
                .stream()
                .map(appointmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getDoctorAppointmentsByStatus(Long doctorId, AppointmentStatus status) {
        return appointmentRepository.findByDoctorIdAndStatusOrderByAppointmentDateTimeAsc(doctorId, status)
                .stream()
                .map(appointmentMapper::toDTO)
                .collect(Collectors.toList());
    }
}
