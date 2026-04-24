package com.hospital.management.services;

import com.hospital.management.dto.DashboardStatsDTO;
import com.hospital.management.dto.DirectorDashboardDTO;
import com.hospital.management.dto.DoctorPerformanceDTO;
import com.hospital.management.dto.DoctorStatsDTO;

import java.util.List;

public interface IStatisticsService {
    DashboardStatsDTO getDashboardStats();
    
    DoctorStatsDTO getDoctorStats(Long doctorId);
    
    DirectorDashboardDTO getDirectorDashboard();
    
    List<DoctorPerformanceDTO> getAllDoctorsPerformance();
    
    DoctorPerformanceDTO getDoctorPerformance(Long doctorId);
}
