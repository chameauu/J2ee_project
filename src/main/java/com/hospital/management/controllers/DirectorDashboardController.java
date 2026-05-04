package com.hospital.management.controllers;

import com.hospital.management.dto.DirectorDashboardDTO;
import com.hospital.management.dto.DoctorPerformanceDTO;
import com.hospital.management.security.HospitalAuthorizationService;
import com.hospital.management.services.IStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/director")
@RequiredArgsConstructor
public class DirectorDashboardController {

    private final IStatisticsService statisticsService;
    private final HospitalAuthorizationService hospitalAuthorizationService;

    // Phase 10.8: Updated to allow DIRECTOR role and return hospital-specific data
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<DirectorDashboardDTO> getDirectorDashboard(Authentication authentication) {
        String userEmail = authentication.getName();
        Long hospitalId = hospitalAuthorizationService.getUserHospitalId(userEmail)
                .orElseThrow(() -> new RuntimeException("User has no hospital assigned"));
        
        return ResponseEntity.ok(statisticsService.getDirectorDashboardByHospital(hospitalId));
    }

    @GetMapping("/doctors/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<List<DoctorPerformanceDTO>> getAllDoctorsPerformance(Authentication authentication) {
        String userEmail = authentication.getName();
        Long hospitalId = hospitalAuthorizationService.getUserHospitalId(userEmail)
                .orElseThrow(() -> new RuntimeException("User has no hospital assigned"));
        
        return ResponseEntity.ok(statisticsService.getDoctorsPerformanceByHospital(hospitalId));
    }

    @GetMapping("/doctors/{doctorId}/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<DoctorPerformanceDTO> getDoctorPerformance(@PathVariable Long doctorId) {
        return ResponseEntity.ok(statisticsService.getDoctorPerformance(doctorId));
    }
}
