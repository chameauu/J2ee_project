package com.hospital.management.controllers;

import com.hospital.management.dto.MedicalRecordDTO;
import com.hospital.management.services.IMedicalRecordService;
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
public class MedicalRecordController {

    private final IMedicalRecordService medicalRecordService;

    @PostMapping("/medical-records")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<MedicalRecordDTO> createMedicalRecord(@Valid @RequestBody MedicalRecordDTO dto) {
        MedicalRecordDTO created = medicalRecordService.createMedicalRecord(dto);
        return ResponseEntity.created(URI.create("/api/medical-records/" + created.getId())).body(created);
    }

    @GetMapping("/medical-records/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<MedicalRecordDTO> getMedicalRecord(@PathVariable Long id) {
        MedicalRecordDTO record = medicalRecordService.getMedicalRecordById(id);
        return ResponseEntity.ok(record);
    }

    @PutMapping("/medical-records/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<MedicalRecordDTO> updateMedicalRecord(
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordDTO dto) {
        MedicalRecordDTO updated = medicalRecordService.updateMedicalRecord(id, dto);
        return ResponseEntity.ok(updated);
    }

    // Phase 10.5: Hospital-scoped authorization for patient medical records
    @GetMapping("/patients/{patientId}/medical-records")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT') and " +
                  "@hospitalAuthorizationService.canAccessUserData(#patientId, authentication)")
    public ResponseEntity<List<MedicalRecordDTO>> getPatientMedicalHistory(@PathVariable Long patientId) {
        List<MedicalRecordDTO> records = medicalRecordService.getPatientMedicalHistory(patientId);
        return ResponseEntity.ok(records);
    }

    // Phase 10.5: Hospital-scoped authorization for doctor medical records
    @GetMapping("/doctors/{doctorId}/medical-records")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR') and " +
                  "@hospitalAuthorizationService.canAccessUserData(#doctorId, authentication)")
    public ResponseEntity<List<MedicalRecordDTO>> getDoctorMedicalRecords(@PathVariable Long doctorId) {
        List<MedicalRecordDTO> records = medicalRecordService.getDoctorMedicalRecords(doctorId);
        return ResponseEntity.ok(records);
    }
}
