package com.hospital.management.services;

import com.hospital.management.dto.AppointmentDTO;
import com.hospital.management.enums.AppointmentStatus;

import java.util.List;

public interface IAppointmentService {

    AppointmentDTO createAppointment(AppointmentDTO dto);

    AppointmentDTO getAppointmentById(Long id);

    AppointmentDTO updateAppointmentStatus(Long id, AppointmentStatus status);

    AppointmentDTO cancelAppointment(Long id);

    List<AppointmentDTO> getPatientAppointments(Long patientId);

    List<AppointmentDTO> getDoctorAppointments(Long doctorId);

    List<AppointmentDTO> getDoctorAppointmentsByStatus(Long doctorId, AppointmentStatus status);
}
