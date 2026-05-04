package com.hospital.management.services;

import com.hospital.management.dto.*;
import com.hospital.management.entities.Doctor;
import com.hospital.management.entities.Hospital;
import com.hospital.management.enums.AppointmentStatus;
import com.hospital.management.enums.PrescriptionStatus;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements IStatisticsService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PharmacistRepository pharmacistRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final HospitalRepository hospitalRepository;

    @Override
    public DashboardStatsDTO getDashboardStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return DashboardStatsDTO.builder()
                .totalDoctors(doctorRepository.count())
                .totalPatients(patientRepository.count())
                .totalPharmacists(pharmacistRepository.count())
                .todaysAppointments(appointmentRepository.countByAppointmentDateTimeBetween(startOfDay, endOfDay))
                .completedAppointments(appointmentRepository.countByStatus(AppointmentStatus.COMPLETED))
                .activePrescriptions(prescriptionRepository.countByStatus(PrescriptionStatus.ACTIVE))
                .totalMedicalRecords(medicalRecordRepository.count())
                .build();
    }

    @Override
    public DoctorStatsDTO getDoctorStats(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        String doctorName = doctor.getFirstName() + " " + doctor.getLastName();

        return DoctorStatsDTO.builder()
                .doctorId(doctorId)
                .doctorName(doctorName)
                .totalPatients(medicalRecordRepository.countDistinctPatientsByDoctorId(doctorId))
                .totalAppointments(appointmentRepository.countByDoctorId(doctorId))
                .todaysAppointments(appointmentRepository.countByDoctorIdAndAppointmentDateTimeBetween(doctorId, startOfDay, endOfDay))
                .completedAppointments(appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.COMPLETED))
                .totalMedicalRecords(medicalRecordRepository.countByDoctorId(doctorId))
                .totalPrescriptions(prescriptionRepository.countByDoctorId(doctorId))
                .build();
    }

    @Override
    public DirectorDashboardDTO getDirectorDashboard() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // Get counts
        Long totalDoctors = doctorRepository.count();
        Long totalAppointments = appointmentRepository.count();
        Long completedAppointments = appointmentRepository.countByStatus(AppointmentStatus.COMPLETED);
        Long scheduledAppointments = appointmentRepository.countByStatus(AppointmentStatus.SCHEDULED);
        Long cancelledAppointments = appointmentRepository.countByStatus(AppointmentStatus.CANCELLED);
        Long todaysAppointments = appointmentRepository.countByAppointmentDateTimeBetween(startOfDay, endOfDay);
        Long todaysCompleted = appointmentRepository.countByStatusAndAppointmentDateTimeBetween(
                AppointmentStatus.COMPLETED, startOfDay, endOfDay);

        // Calculate KPIs
        Double appointmentCompletionRate = totalAppointments > 0 
                ? (completedAppointments.doubleValue() / totalAppointments.doubleValue()) * 100 
                : 0.0;
        
        Double averageAppointmentsPerDoctor = totalDoctors > 0 
                ? totalAppointments.doubleValue() / totalDoctors.doubleValue() 
                : 0.0;

        // Calculate doctor utilization (doctors with appointments today / total doctors)
        Long doctorsWithAppointmentsToday = appointmentRepository.countDistinctDoctorsByAppointmentDateTimeBetween(startOfDay, endOfDay);
        Double doctorUtilizationRate = totalDoctors > 0 
                ? (doctorsWithAppointmentsToday.doubleValue() / totalDoctors.doubleValue()) * 100 
                : 0.0;

        // Average patients per doctor (unique patients)
        Long totalUniquePatients = medicalRecordRepository.countDistinctPatients();
        Double averagePatientsPerDoctor = totalDoctors > 0 
                ? totalUniquePatients.doubleValue() / totalDoctors.doubleValue() 
                : 0.0;

        return DirectorDashboardDTO.builder()
                .totalDoctors(totalDoctors)
                .totalPatients(patientRepository.count())
                .totalPharmacists(pharmacistRepository.count())
                .totalAppointments(totalAppointments)
                .totalMedicalRecords(medicalRecordRepository.count())
                .totalPrescriptions(prescriptionRepository.count())
                .appointmentCompletionRate(Math.round(appointmentCompletionRate * 100.0) / 100.0)
                .doctorUtilizationRate(Math.round(doctorUtilizationRate * 100.0) / 100.0)
                .averageAppointmentsPerDoctor(Math.round(averageAppointmentsPerDoctor * 100.0) / 100.0)
                .averagePatientsPerDoctor(Math.round(averagePatientsPerDoctor * 100.0) / 100.0)
                .todaysAppointments(todaysAppointments)
                .todaysCompletedAppointments(todaysCompleted)
                .scheduledAppointments(scheduledAppointments)
                .completedAppointments(completedAppointments)
                .cancelledAppointments(cancelledAppointments)
                .activePrescriptions(prescriptionRepository.countByStatus(PrescriptionStatus.ACTIVE))
                .dispensedPrescriptions(prescriptionRepository.countByStatus(PrescriptionStatus.DISPENSED))
                .build();
    }

    // Phase 10.8: Hospital-specific director dashboard
    @Override
    public DirectorDashboardDTO getDirectorDashboardByHospital(Long hospitalId) {
        // Get hospital details
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + hospitalId));
        
        // Get counts for specific hospital using existing methods
        Long totalDoctors = doctorRepository.countByHospitalId(hospitalId);
        Long totalAppointments = appointmentRepository.countByHospitalId(hospitalId);
        Long completedAppointments = appointmentRepository.countByHospitalIdAndStatus(hospitalId, AppointmentStatus.COMPLETED);
        Long scheduledAppointments = appointmentRepository.countByHospitalIdAndStatus(hospitalId, AppointmentStatus.SCHEDULED);
        Long cancelledAppointments = appointmentRepository.countByHospitalIdAndStatus(hospitalId, AppointmentStatus.CANCELLED);

        // Calculate KPIs
        Double appointmentCompletionRate = totalAppointments > 0 
                ? (completedAppointments.doubleValue() / totalAppointments.doubleValue()) * 100 
                : 0.0;
        
        Double averageAppointmentsPerDoctor = totalDoctors > 0 
                ? totalAppointments.doubleValue() / totalDoctors.doubleValue() 
                : 0.0;

        return DirectorDashboardDTO.builder()
                .hospitalId(hospitalId)
                .hospitalName(hospital.getName())
                .totalDoctors(totalDoctors)
                .totalPatients(patientRepository.countByHospitalId(hospitalId))
                .totalPharmacists(pharmacistRepository.countByHospitalId(hospitalId))
                .totalAppointments(totalAppointments)
                .totalMedicalRecords(medicalRecordRepository.countByHospitalId(hospitalId))
                .totalPrescriptions(prescriptionRepository.countByHospitalId(hospitalId))
                .appointmentCompletionRate(Math.round(appointmentCompletionRate * 100.0) / 100.0)
                .doctorUtilizationRate(0.0)
                .averageAppointmentsPerDoctor(Math.round(averageAppointmentsPerDoctor * 100.0) / 100.0)
                .averagePatientsPerDoctor(0.0)
                .todaysAppointments(0L)
                .todaysCompletedAppointments(0L)
                .scheduledAppointments(scheduledAppointments)
                .completedAppointments(completedAppointments)
                .cancelledAppointments(cancelledAppointments)
                .activePrescriptions(prescriptionRepository.countByHospitalIdAndStatus(hospitalId, PrescriptionStatus.ACTIVE))
                .dispensedPrescriptions(prescriptionRepository.countByHospitalIdAndStatus(hospitalId, PrescriptionStatus.DISPENSED))
                .build();
    }

    @Override
    public List<DoctorPerformanceDTO> getAllDoctorsPerformance() {
        List<Doctor> doctors = doctorRepository.findAll();
        return doctors.stream()
                .map(this::buildDoctorPerformance)
                .collect(Collectors.toList());
    }

    // Phase 10.8: Hospital-specific doctor performance
    @Override
    public List<DoctorPerformanceDTO> getDoctorsPerformanceByHospital(Long hospitalId) {
        List<Doctor> doctors = doctorRepository.findByHospitalId(hospitalId);
        return doctors.stream()
                .map(this::buildDoctorPerformance)
                .collect(Collectors.toList());
    }

    @Override
    public DoctorPerformanceDTO getDoctorPerformance(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));
        return buildDoctorPerformance(doctor);
    }

    private DoctorPerformanceDTO buildDoctorPerformance(Doctor doctor) {
        Long doctorId = doctor.getId();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        Long totalAppointments = appointmentRepository.countByDoctorId(doctorId);
        Long completedAppointments = appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.COMPLETED);
        Long cancelledAppointments = appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.CANCELLED);

        Double completionRate = totalAppointments > 0 
                ? (completedAppointments.doubleValue() / totalAppointments.doubleValue()) * 100 
                : 0.0;

        // Utilization rate: completed appointments / total possible appointments (assume 8 appointments per day * 30 days)
        Long possibleAppointments = 240L; // 8 per day * 30 days
        Double utilizationRate = (completedAppointments.doubleValue() / possibleAppointments.doubleValue()) * 100;

        return DoctorPerformanceDTO.builder()
                .doctorId(doctorId)
                .doctorName(doctor.getFirstName() + " " + doctor.getLastName())
                .specialization(doctor.getSpecialization())
                .totalPatients(medicalRecordRepository.countDistinctPatientsByDoctorId(doctorId))
                .totalAppointments(totalAppointments)
                .completedAppointments(completedAppointments)
                .cancelledAppointments(cancelledAppointments)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .totalMedicalRecords(medicalRecordRepository.countByDoctorId(doctorId))
                .totalPrescriptions(prescriptionRepository.countByDoctorId(doctorId))
                .todaysAppointments(appointmentRepository.countByDoctorIdAndAppointmentDateTimeBetween(doctorId, startOfDay, endOfDay))
                .utilizationRate(Math.round(utilizationRate * 100.0) / 100.0)
                .build();
    }
}
