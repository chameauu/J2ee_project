package com.hospital.management.controllers;

import com.hospital.management.dto.DoctorDTO;
import com.hospital.management.services.IDoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final IDoctorService doctorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorDTO> createDoctor(@Valid @RequestBody DoctorDTO doctorDTO) {
        DoctorDTO created = doctorService.createDoctor(doctorDTO);
        return ResponseEntity.created(URI.create("/api/doctors/" + created.getId())).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('DOCTOR') and @hospitalAuthorizationService.isOwner(#id, authentication))")
    public ResponseEntity<DoctorDTO> getDoctor(@PathVariable Long id) {
        DoctorDTO doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctor);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('DOCTOR') and @hospitalAuthorizationService.isOwner(#id, authentication))")
    public ResponseEntity<DoctorDTO> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorDTO doctorDTO) {
        DoctorDTO updated = doctorService.updateDoctor(id, doctorDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
        List<DoctorDTO> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<DoctorDTO>> searchBySpecialization(
            @RequestParam String specialization) {
        List<DoctorDTO> doctors = doctorService.getDoctorsBySpecialization(specialization);
        return ResponseEntity.ok(doctors);
    }

    // Phase 10.3: Hospital-scoped queries
    // Phase 10.4: Added authorization checks
    // Phase 10.11: Removed patient access
    @GetMapping("/hospital/{hospitalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'DOCTOR') and " +
                  "@hospitalAuthorizationService.canAccessHospital(#hospitalId, authentication)")
    public ResponseEntity<List<DoctorDTO>> getDoctorsByHospital(
            @PathVariable Long hospitalId,
            @RequestParam(required = false) String specialization) {
        List<DoctorDTO> doctors;
        if (specialization != null && !specialization.isEmpty()) {
            doctors = doctorService.getDoctorsByHospitalAndSpecialization(hospitalId, specialization);
        } else {
            doctors = doctorService.getDoctorsByHospital(hospitalId);
        }
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/hospital/{hospitalId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR') and " +
                  "@hospitalAuthorizationService.canAccessHospital(#hospitalId, authentication)")
    public ResponseEntity<Long> countDoctorsByHospital(@PathVariable Long hospitalId) {
        Long count = doctorService.countDoctorsByHospital(hospitalId);
        return ResponseEntity.ok(count);
    }
}
