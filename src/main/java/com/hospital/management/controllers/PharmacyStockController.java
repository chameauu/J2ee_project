package com.hospital.management.controllers;

import com.hospital.management.dto.PharmacyStockDTO;
import com.hospital.management.services.IPharmacyStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy/stock")
@RequiredArgsConstructor
public class PharmacyStockController {

    private final IPharmacyStockService pharmacyStockService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PharmacyStockDTO> createStock(@Valid @RequestBody PharmacyStockDTO dto) {
        PharmacyStockDTO created = pharmacyStockService.createStock(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<PharmacyStockDTO> getStockById(@PathVariable Long id) {
        PharmacyStockDTO stock = pharmacyStockService.getStockById(id);
        return ResponseEntity.ok(stock);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<PharmacyStockDTO>> getAllStock() {
        List<PharmacyStockDTO> stocks = pharmacyStockService.getAllStock();
        return ResponseEntity.ok(stocks);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PharmacyStockDTO> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody PharmacyStockDTO dto) {
        PharmacyStockDTO updated = pharmacyStockService.updateStock(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        pharmacyStockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/medication/{medicationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<PharmacyStockDTO>> getStockByMedicationId(@PathVariable Long medicationId) {
        List<PharmacyStockDTO> stocks = pharmacyStockService.getStockByMedicationId(medicationId);
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/low")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<PharmacyStockDTO>> getLowStockItems() {
        List<PharmacyStockDTO> stocks = pharmacyStockService.getLowStockItems();
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<PharmacyStockDTO>> getExpiringSoonItems() {
        List<PharmacyStockDTO> stocks = pharmacyStockService.getExpiringSoonItems();
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/expired")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<PharmacyStockDTO>> getExpiredStock() {
        List<PharmacyStockDTO> stocks = pharmacyStockService.getExpiredStock();
        return ResponseEntity.ok(stocks);
    }

    @PutMapping("/{id}/reduce")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<PharmacyStockDTO> reduceStock(
            @PathVariable Long id,
            @RequestParam Integer amount) {
        PharmacyStockDTO updated = pharmacyStockService.reduceStock(id, amount);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PharmacyStockDTO> addStock(
            @PathVariable Long id,
            @RequestParam Integer amount) {
        PharmacyStockDTO updated = pharmacyStockService.addStock(id, amount);
        return ResponseEntity.ok(updated);
    }
}
