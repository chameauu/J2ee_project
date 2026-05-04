package com.hospital.management.controllers;

import com.hospital.management.dto.PrescriptionDTO;
import com.hospital.management.enums.PrescriptionStatus;
import com.hospital.management.services.IPrescriptionService;
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
public class PrescriptionController {

    private final IPrescriptionService prescriptionService;

    @PostMapping("/prescriptions")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionDTO> createPrescription(@Valid @RequestBody PrescriptionDTO dto) {
        PrescriptionDTO created = prescriptionService.createPrescription(dto);
        return ResponseEntity.created(URI.create("/api/prescriptions/" + created.getId())).body(created);
    }

    @GetMapping("/prescriptions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST', 'PATIENT')")
    public ResponseEntity<PrescriptionDTO> getPrescription(@PathVariable Long id) {
        PrescriptionDTO prescription = prescriptionService.getPrescriptionById(id);
        return ResponseEntity.ok(prescription);
    }

    @PutMapping("/prescriptions/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionDTO> updatePrescription(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionDTO dto) {
        PrescriptionDTO updated = prescriptionService.updatePrescription(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/prescriptions/{id}/status")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PHARMACIST')")
    public ResponseEntity<PrescriptionDTO> updatePrescriptionStatus(
            @PathVariable Long id,
            @RequestParam PrescriptionStatus status) {
        PrescriptionDTO updated = prescriptionService.updatePrescriptionStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/prescriptions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Void> deletePrescription(@PathVariable Long id) {
        prescriptionService.deletePrescription(id);
        return ResponseEntity.noContent().build();
    }

    // Phase 10.5: Hospital-scoped authorization for patient prescriptions
    @GetMapping("/patients/{patientId}/prescriptions")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST', 'PATIENT') and " +
                  "@hospitalAuthorizationService.canAccessUserData(#patientId, authentication)")
    public ResponseEntity<List<PrescriptionDTO>> getPatientPrescriptions(@PathVariable Long patientId) {
        List<PrescriptionDTO> prescriptions = prescriptionService.getPatientPrescriptions(patientId);
        return ResponseEntity.ok(prescriptions);
    }

    // Phase 10.5: Hospital-scoped authorization for patient active prescriptions
    @GetMapping("/patients/{patientId}/prescriptions/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST', 'PATIENT') and " +
                  "@hospitalAuthorizationService.canAccessUserData(#patientId, authentication)")
    public ResponseEntity<List<PrescriptionDTO>> getPatientActivePrescriptions(@PathVariable Long patientId) {
        List<PrescriptionDTO> prescriptions = prescriptionService.getPatientActivePrescriptions(patientId);
        return ResponseEntity.ok(prescriptions);
    }

    // Phase 10.5: Hospital-scoped authorization for doctor prescriptions
    @GetMapping("/doctors/{doctorId}/prescriptions")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR') and " +
                  "@hospitalAuthorizationService.canAccessUserData(#doctorId, authentication)")
    public ResponseEntity<List<PrescriptionDTO>> getDoctorPrescriptions(@PathVariable Long doctorId) {
        List<PrescriptionDTO> prescriptions = prescriptionService.getDoctorPrescriptions(doctorId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/medical-records/{medicalRecordId}/prescriptions")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST')")
    public ResponseEntity<List<PrescriptionDTO>> getMedicalRecordPrescriptions(@PathVariable Long medicalRecordId) {
        List<PrescriptionDTO> prescriptions = prescriptionService.getMedicalRecordPrescriptions(medicalRecordId);
        return ResponseEntity.ok(prescriptions);
    }
}
