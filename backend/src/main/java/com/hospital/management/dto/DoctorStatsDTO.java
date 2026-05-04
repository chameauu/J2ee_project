package com.hospital.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatsDTO {
    private Long doctorId;
    private String doctorName;
    private Long totalPatients;
    private Long totalAppointments;
    private Long todaysAppointments;
    private Long completedAppointments;
    private Long totalMedicalRecords;
    private Long totalPrescriptions;
}
