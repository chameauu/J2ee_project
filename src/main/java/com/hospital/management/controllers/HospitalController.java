package com.hospital.management.controllers;

import com.hospital.management.dto.HospitalDTO;
import com.hospital.management.services.IHospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final IHospitalService hospitalService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HospitalDTO> createHospital(@Valid @RequestBody HospitalDTO hospitalDTO) {
        HospitalDTO createdHospital = hospitalService.createHospital(hospitalDTO);
        return new ResponseEntity<>(createdHospital, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<HospitalDTO> getHospitalById(@PathVariable Long id) {
        HospitalDTO hospital = hospitalService.getHospitalById(id);
        return ResponseEntity.ok(hospital);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HospitalDTO> updateHospital(
            @PathVariable Long id,
            @Valid @RequestBody HospitalDTO hospitalDTO) {
        HospitalDTO updatedHospital = hospitalService.updateHospital(id, hospitalDTO);
        return ResponseEntity.ok(updatedHospital);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHospital(@PathVariable Long id) {
        hospitalService.deleteHospital(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<List<HospitalDTO>> getAllHospitals() {
        List<HospitalDTO> hospitals = hospitalService.getAllHospitals();
        return ResponseEntity.ok(hospitals);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HospitalDTO>> searchHospitals(@RequestParam String keyword) {
        List<HospitalDTO> hospitals = hospitalService.searchHospitals(keyword);
        return ResponseEntity.ok(hospitals);
    }
}
