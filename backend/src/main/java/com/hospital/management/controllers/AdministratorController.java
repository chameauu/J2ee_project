package com.hospital.management.controllers;

import com.hospital.management.dto.AdministratorDTO;
import com.hospital.management.services.IAdministratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/administrators")
@RequiredArgsConstructor
public class AdministratorController {

    private final IAdministratorService administratorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdministratorDTO> createAdministrator(@Valid @RequestBody AdministratorDTO dto) {
        AdministratorDTO created = administratorService.createAdministrator(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdministratorDTO> getAdministratorById(@PathVariable Long id) {
        AdministratorDTO administrator = administratorService.getAdministratorById(id);
        return ResponseEntity.ok(administrator);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdministratorDTO> updateAdministrator(@PathVariable Long id, @Valid @RequestBody AdministratorDTO dto) {
        AdministratorDTO updated = administratorService.updateAdministrator(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAdministrator(@PathVariable Long id) {
        administratorService.deleteAdministrator(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdministratorDTO>> getAllAdministrators() {
        List<AdministratorDTO> administrators = administratorService.getAllAdministrators();
        return ResponseEntity.ok(administrators);
    }

    // Phase 10.3: Hospital-scoped queries
    // Phase 10.4: Added authorization checks
    @GetMapping("/hospital/{hospitalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR') and " +
                  "@hospitalAuthorizationService.canAccessHospital(#hospitalId, authentication)")
    public ResponseEntity<List<AdministratorDTO>> getAdministratorsByHospital(@PathVariable Long hospitalId) {
        List<AdministratorDTO> administrators = administratorService.getAdministratorsByHospital(hospitalId);
        return ResponseEntity.ok(administrators);
    }

    @GetMapping("/hospital/{hospitalId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR') and " +
                  "@hospitalAuthorizationService.canAccessHospital(#hospitalId, authentication)")
    public ResponseEntity<Long> countAdministratorsByHospital(@PathVariable Long hospitalId) {
        Long count = administratorService.countAdministratorsByHospital(hospitalId);
        return ResponseEntity.ok(count);
    }
}
