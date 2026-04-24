package com.hospital.management.services;

import com.hospital.management.dto.DashboardStatsDTO;
import com.hospital.management.dto.DoctorStatsDTO;

public interface IStatisticsService {
    DashboardStatsDTO getDashboardStats();
    
    DoctorStatsDTO getDoctorStats(Long doctorId);
}
