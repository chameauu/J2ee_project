package com.hospital.management.controllers;

import com.hospital.management.dto.DirectorDashboardDTO;
import com.hospital.management.dto.DoctorPerformanceDTO;
import com.hospital.management.services.IStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DirectorDashboardDTO> getDirectorDashboard() {
        return ResponseEntity.ok(statisticsService.getDirectorDashboard());
    }

    @GetMapping("/doctors/performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DoctorPerformanceDTO>> getAllDoctorsPerformance() {
        return ResponseEntity.ok(statisticsService.getAllDoctorsPerformance());
    }

    @GetMapping("/doctors/{doctorId}/performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorPerformanceDTO> getDoctorPerformance(@PathVariable Long doctorId) {
        return ResponseEntity.ok(statisticsService.getDoctorPerformance(doctorId));
    }
}
