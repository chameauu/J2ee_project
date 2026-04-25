package com.hospital.management.controllers;

import com.hospital.management.dto.PrescriptionItemDTO;
import com.hospital.management.services.IPrescriptionItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescription-items")
@RequiredArgsConstructor
public class PrescriptionItemController {

    private final IPrescriptionItemService prescriptionItemService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionItemDTO> createPrescriptionItem(@Valid @RequestBody PrescriptionItemDTO dto) {
        PrescriptionItemDTO created = prescriptionItemService.createPrescriptionItem(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST')")
    public ResponseEntity<PrescriptionItemDTO> getPrescriptionItemById(@PathVariable Long id) {
        PrescriptionItemDTO item = prescriptionItemService.getPrescriptionItemById(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/prescription/{prescriptionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST', 'PATIENT')")
    public ResponseEntity<List<PrescriptionItemDTO>> getItemsByPrescriptionId(@PathVariable Long prescriptionId) {
        List<PrescriptionItemDTO> items = prescriptionItemService.getItemsByPrescriptionId(prescriptionId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/medication/{medicationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<PrescriptionItemDTO>> getItemsByMedicationId(@PathVariable Long medicationId) {
        List<PrescriptionItemDTO> items = prescriptionItemService.getItemsByMedicationId(medicationId);
        return ResponseEntity.ok(items);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionItemDTO> updatePrescriptionItem(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionItemDTO dto) {
        PrescriptionItemDTO updated = prescriptionItemService.updatePrescriptionItem(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deletePrescriptionItem(@PathVariable Long id) {
        prescriptionItemService.deletePrescriptionItem(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/dispense")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<PrescriptionItemDTO> dispenseItem(
            @PathVariable Long id,
            @RequestParam Long pharmacistId) {
        PrescriptionItemDTO dispensed = prescriptionItemService.dispenseItem(id, pharmacistId);
        return ResponseEntity.ok(dispensed);
    }

    @GetMapping("/prescription/{prescriptionId}/undispensed")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<PrescriptionItemDTO>> getUndispensedItems(@PathVariable Long prescriptionId) {
        List<PrescriptionItemDTO> items = prescriptionItemService.getUndispensedItemsByPrescriptionId(prescriptionId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/pharmacist/{pharmacistId}/dispensed")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<PrescriptionItemDTO>> getDispensedItemsByPharmacist(@PathVariable Long pharmacistId) {
        List<PrescriptionItemDTO> items = prescriptionItemService.getDispensedItemsByPharmacistId(pharmacistId);
        return ResponseEntity.ok(items);
    }
}
