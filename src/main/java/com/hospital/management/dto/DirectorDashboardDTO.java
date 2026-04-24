package com.hospital.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectorDashboardDTO {
    // System Overview
    private Long totalDoctors;
    private Long totalPatients;
    private Long totalPharmacists;
    private Long totalAppointments;
    private Long totalMedicalRecords;
    private Long totalPrescriptions;
    
    // KPIs
    private Double appointmentCompletionRate;
    private Double doctorUtilizationRate;
    private Double averageAppointmentsPerDoctor;
    private Double averagePatientsPerDoctor;
    
    // Today's Metrics
    private Long todaysAppointments;
    private Long todaysCompletedAppointments;
    
    // Status Breakdown
    private Long scheduledAppointments;
    private Long completedAppointments;
    private Long cancelledAppointments;
    private Long activePrescriptions;
    private Long dispensedPrescriptions;
}
