package com.hospital.management.controllers;

import com.hospital.management.dto.PharmacistDTO;
import com.hospital.management.services.IPharmacistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pharmacists")
@RequiredArgsConstructor
public class PharmacistController {

    private final IPharmacistService pharmacistService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PharmacistDTO> createPharmacist(@Valid @RequestBody PharmacistDTO pharmacistDTO) {
        PharmacistDTO created = pharmacistService.createPharmacist(pharmacistDTO);
        return ResponseEntity.created(URI.create("/api/pharmacists/" + created.getId())).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<PharmacistDTO> getPharmacist(@PathVariable Long id) {
        PharmacistDTO pharmacist = pharmacistService.getPharmacistById(id);
        return ResponseEntity.ok(pharmacist);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<PharmacistDTO> updatePharmacist(
            @PathVariable Long id,
            @Valid @RequestBody PharmacistDTO pharmacistDTO) {
        PharmacistDTO updated = pharmacistService.updatePharmacist(id, pharmacistDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePharmacist(@PathVariable Long id) {
        pharmacistService.deletePharmacist(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST')")
    public ResponseEntity<List<PharmacistDTO>> getAllPharmacists() {
        List<PharmacistDTO> pharmacists = pharmacistService.getAllPharmacists();
        return ResponseEntity.ok(pharmacists);
    }

    // Phase 10.3: Hospital-scoped queries
    // Phase 10.4: Added authorization checks
    @GetMapping("/hospital/{hospitalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PHARMACIST') and " +
                  "@hospitalAuthorizationService.canAccessHospital(#hospitalId, authentication)")
    public ResponseEntity<List<PharmacistDTO>> getPharmacistsByHospital(@PathVariable Long hospitalId) {
        List<PharmacistDTO> pharmacists = pharmacistService.getPharmacistsByHospital(hospitalId);
        return ResponseEntity.ok(pharmacists);
    }

    @GetMapping("/hospital/{hospitalId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR') and " +
                  "@hospitalAuthorizationService.canAccessHospital(#hospitalId, authentication)")
    public ResponseEntity<Long> countPharmacistsByHospital(@PathVariable Long hospitalId) {
        Long count = pharmacistService.countPharmacistsByHospital(hospitalId);
        return ResponseEntity.ok(count);
    }
}
