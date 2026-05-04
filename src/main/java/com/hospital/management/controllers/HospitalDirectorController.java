package com.hospital.management.controllers;

import com.hospital.management.dto.HospitalDirectorDTO;
import com.hospital.management.services.IHospitalDirectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital-directors")
@RequiredArgsConstructor
public class HospitalDirectorController {

    private final IHospitalDirectorService hospitalDirectorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HospitalDirectorDTO> createHospitalDirector(@Valid @RequestBody HospitalDirectorDTO dto) {
        HospitalDirectorDTO created = hospitalDirectorService.createHospitalDirector(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('DIRECTOR') and @hospitalAuthorizationService.isOwner(#id, authentication))")
    public ResponseEntity<HospitalDirectorDTO> getHospitalDirectorById(@PathVariable Long id) {
        HospitalDirectorDTO director = hospitalDirectorService.getHospitalDirectorById(id);
        return ResponseEntity.ok(director);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<List<HospitalDirectorDTO>> getAllHospitalDirectors() {
        List<HospitalDirectorDTO> directors = hospitalDirectorService.getAllHospitalDirectors();
        return ResponseEntity.ok(directors);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HospitalDirectorDTO> updateHospitalDirector(
            @PathVariable Long id,
            @Valid @RequestBody HospitalDirectorDTO dto) {
        HospitalDirectorDTO updated = hospitalDirectorService.updateHospitalDirector(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHospitalDirector(@PathVariable Long id) {
        hospitalDirectorService.deleteHospitalDirector(id);
        return ResponseEntity.noContent().build();
    }

    // Phase 10.3: Hospital-scoped queries
    // Phase 10.4: Added authorization checks
    @GetMapping("/hospital/{hospitalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR') and " +
                  "@hospitalAuthorizationService.canAccessHospital(#hospitalId, authentication)")
    public ResponseEntity<List<HospitalDirectorDTO>> getHospitalDirectorsByHospital(@PathVariable Long hospitalId) {
        List<HospitalDirectorDTO> directors = hospitalDirectorService.getHospitalDirectorsByHospital(hospitalId);
        return ResponseEntity.ok(directors);
    }

    @GetMapping("/hospital/{hospitalId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR') and " +
                  "@hospitalAuthorizationService.canAccessHospital(#hospitalId, authentication)")
    public ResponseEntity<Long> countHospitalDirectorsByHospital(@PathVariable Long hospitalId) {
        Long count = hospitalDirectorService.countHospitalDirectorsByHospital(hospitalId);
        return ResponseEntity.ok(count);
    }
}
