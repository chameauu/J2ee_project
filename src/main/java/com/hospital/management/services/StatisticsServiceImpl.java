package com.hospital.management.services;

import com.hospital.management.dto.DashboardStatsDTO;
import com.hospital.management.dto.DoctorStatsDTO;
import com.hospital.management.entities.Doctor;
import com.hospital.management.enums.AppointmentStatus;
import com.hospital.management.enums.PrescriptionStatus;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
}
