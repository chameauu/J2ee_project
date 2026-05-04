package com.hospital.management.controllers;

import com.hospital.management.dto.PatientDTO;
import com.hospital.management.services.IPatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final IPatientService patientService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientDTO patientDTO) {
        PatientDTO created = patientService.createPatient(patientDTO);
        return ResponseEntity.created(URI.create("/api/patients/" + created.getId())).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST', 'PATIENT')")
    public ResponseEntity<PatientDTO> getPatient(@PathVariable Long id) {
        PatientDTO patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientDTO patientDTO) {
        PatientDTO updated = patientService.updatePatient(id, patientDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST')")
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        List<PatientDTO> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    // Phase 10.3: Hospital-scoped queries
    @GetMapping("/hospital/{hospitalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'DOCTOR', 'PHARMACIST')")
    public ResponseEntity<List<PatientDTO>> getPatientsByHospital(@PathVariable Long hospitalId) {
        List<PatientDTO> patients = patientService.getPatientsByHospital(hospitalId);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/hospital/{hospitalId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<Long> countPatientsByHospital(@PathVariable Long hospitalId) {
        Long count = patientService.countPatientsByHospital(hospitalId);
        return ResponseEntity.ok(count);
    }
}
