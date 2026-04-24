package com.hospital.management.controllers;

import com.hospital.management.dto.AppointmentDTO;
import com.hospital.management.enums.AppointmentStatus;
import com.hospital.management.services.IAppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AppointmentController {

    private final IAppointmentService appointmentService;

    @PostMapping("/appointments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppointmentDTO> createAppointment(@Valid @RequestBody AppointmentDTO dto) {
        AppointmentDTO created = appointmentService.createAppointment(dto);
        return ResponseEntity.created(URI.create("/api/appointments/" + created.getId())).body(created);
    }

    @GetMapping("/appointments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<AppointmentDTO> getAppointment(@PathVariable Long id) {
        AppointmentDTO appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointment);
    }

    @PutMapping("/appointments/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<AppointmentDTO> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {
        AppointmentDTO updated = appointmentService.updateAppointmentStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/appointments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public ResponseEntity<AppointmentDTO> cancelAppointment(@PathVariable Long id) {
        AppointmentDTO cancelled = appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(cancelled);
    }

    @GetMapping("/patients/{patientId}/appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<List<AppointmentDTO>> getPatientAppointments(@PathVariable Long patientId) {
        List<AppointmentDTO> appointments = appointmentService.getPatientAppointments(patientId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/doctors/{doctorId}/appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<AppointmentDTO>> getDoctorAppointments(
            @PathVariable Long doctorId,
            @RequestParam(required = false) AppointmentStatus status) {
        
        List<AppointmentDTO> appointments;
        if (status != null) {
            appointments = appointmentService.getDoctorAppointmentsByStatus(doctorId, status);
        } else {
            appointments = appointmentService.getDoctorAppointments(doctorId);
        }
        return ResponseEntity.ok(appointments);
    }
}
