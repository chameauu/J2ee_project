package com.hospital.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPerformanceDTO {
    private Long doctorId;
    private String doctorName;
    private String specialization;
    
    // Performance Metrics
    private Long totalPatients;
    private Long totalAppointments;
    private Long completedAppointments;
    private Long cancelledAppointments;
    private Double completionRate;
    
    // Activity Metrics
    private Long totalMedicalRecords;
    private Long totalPrescriptions;
    private Long todaysAppointments;
    
    // Utilization
    private Double utilizationRate;
}
