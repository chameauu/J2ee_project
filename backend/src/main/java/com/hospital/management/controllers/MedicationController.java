package com.hospital.management.controllers;

import com.hospital.management.dto.MedicationDTO;
import com.hospital.management.enums.MedicationType;
import com.hospital.management.services.IMedicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final IMedicationService medicationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicationDTO> createMedication(@Valid @RequestBody MedicationDTO dto) {
        MedicationDTO created = medicationService.createMedication(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST')")
    public ResponseEntity<MedicationDTO> getMedicationById(@PathVariable Long id) {
        MedicationDTO medication = medicationService.getMedicationById(id);
        return ResponseEntity.ok(medication);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST')")
    public ResponseEntity<List<MedicationDTO>> getAllMedications() {
        List<MedicationDTO> medications = medicationService.getAllMedications();
        return ResponseEntity.ok(medications);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicationDTO> updateMedication(
            @PathVariable Long id,
            @Valid @RequestBody MedicationDTO dto) {
        MedicationDTO updated = medicationService.updateMedication(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMedication(@PathVariable Long id) {
        medicationService.deleteMedication(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST')")
    public ResponseEntity<List<MedicationDTO>> searchMedications(@RequestParam String keyword) {
        List<MedicationDTO> medications = medicationService.searchMedications(keyword);
        return ResponseEntity.ok(medications);
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST')")
    public ResponseEntity<List<MedicationDTO>> getMedicationsByType(@PathVariable MedicationType type) {
        List<MedicationDTO> medications = medicationService.getMedicationsByType(type);
        return ResponseEntity.ok(medications);
    }

    @GetMapping("/manufacturer/{manufacturer}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST')")
    public ResponseEntity<List<MedicationDTO>> getMedicationsByManufacturer(@PathVariable String manufacturer) {
        List<MedicationDTO> medications = medicationService.getMedicationsByManufacturer(manufacturer);
        return ResponseEntity.ok(medications);
    }
}
