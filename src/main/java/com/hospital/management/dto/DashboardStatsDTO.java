package com.hospital.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private Long totalDoctors;
    private Long totalPatients;
    private Long totalPharmacists;
    private Long todaysAppointments;
    private Long completedAppointments;
    private Long activePrescriptions;
    private Long totalMedicalRecords;
}
