package com.hospital.management.controllers;

import com.hospital.management.dto.DoctorDTO;
import com.hospital.management.dto.HospitalDTO;
import com.hospital.management.dto.PatientDTO;
import com.hospital.management.services.IDoctorService;
import com.hospital.management.services.IHospitalService;
import com.hospital.management.services.IPatientService;
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
    private final IDoctorService doctorService;
    private final IPatientService patientService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HospitalDTO> createHospital(@Valid @RequestBody HospitalDTO hospitalDTO) {
        HospitalDTO createdHospital = hospitalService.createHospital(hospitalDTO);
        return new ResponseEntity<>(createdHospital, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('DIRECTOR') and @hospitalAuthorizationService.canAccessHospital(#id, authentication))")
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

    // Phase 10.8: Hospital-scoped endpoints for doctors
    @GetMapping("/{hospitalId}/doctors")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DIRECTOR') and @hospitalAuthorizationService.canAccessHospital(#hospitalId, authentication))")
    public ResponseEntity<List<DoctorDTO>> getHospitalDoctors(@PathVariable Long hospitalId) {
        List<DoctorDTO> doctors = doctorService.getDoctorsByHospital(hospitalId);
        return ResponseEntity.ok(doctors);
    }

    // Phase 10.8: Hospital-scoped endpoints for patients
    @GetMapping("/{hospitalId}/patients")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DIRECTOR') and @hospitalAuthorizationService.canAccessHospital(#hospitalId, authentication))")
    public ResponseEntity<List<PatientDTO>> getHospitalPatients(@PathVariable Long hospitalId) {
        List<PatientDTO> patients = patientService.getPatientsByHospital(hospitalId);
        return ResponseEntity.ok(patients);
    }
}
