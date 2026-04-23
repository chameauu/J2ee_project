package com.hospital.management.controllers;

import com.hospital.management.dto.PharmacistDTO;
import com.hospital.management.services.IPharmacistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<PharmacistDTO> createPharmacist(@Valid @RequestBody PharmacistDTO pharmacistDTO) {
        PharmacistDTO created = pharmacistService.createPharmacist(pharmacistDTO);
        return ResponseEntity.created(URI.create("/api/pharmacists/" + created.getId())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PharmacistDTO> getPharmacist(@PathVariable Long id) {
        PharmacistDTO pharmacist = pharmacistService.getPharmacistById(id);
        return ResponseEntity.ok(pharmacist);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PharmacistDTO> updatePharmacist(
            @PathVariable Long id,
            @Valid @RequestBody PharmacistDTO pharmacistDTO) {
        PharmacistDTO updated = pharmacistService.updatePharmacist(id, pharmacistDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePharmacist(@PathVariable Long id) {
        pharmacistService.deletePharmacist(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PharmacistDTO>> getAllPharmacists() {
        List<PharmacistDTO> pharmacists = pharmacistService.getAllPharmacists();
        return ResponseEntity.ok(pharmacists);
    }
}
