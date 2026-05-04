package com.hospital.management.controllers;

import com.hospital.management.dto.DashboardStatsDTO;
import com.hospital.management.dto.DoctorStatsDTO;
import com.hospital.management.services.IStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StatisticsController {

    private final IStatisticsService statisticsService;

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(statisticsService.getDashboardStats());
    }

    // Phase 10.8: System-wide statistics endpoint
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsDTO> getSystemStatistics() {
        return ResponseEntity.ok(statisticsService.getDashboardStats());
    }

    @GetMapping("/doctors/{doctorId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<DoctorStatsDTO> getDoctorStats(@PathVariable Long doctorId) {
        return ResponseEntity.ok(statisticsService.getDoctorStats(doctorId));
    }
}
