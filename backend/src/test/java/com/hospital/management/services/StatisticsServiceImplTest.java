package com.hospital.management.services;

import com.hospital.management.dto.DashboardStatsDTO;
import com.hospital.management.dto.DirectorDashboardDTO;
import com.hospital.management.dto.DoctorPerformanceDTO;
import com.hospital.management.dto.DoctorStatsDTO;
import com.hospital.management.entities.Doctor;
import com.hospital.management.enums.AppointmentStatus;
import com.hospital.management.enums.PrescriptionStatus;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PharmacistRepository pharmacistRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    @BeforeEach
    void setUp() {
        // Mock repository counts - using lenient for flexibility
        lenient().when(doctorRepository.count()).thenReturn(10L);
        lenient().when(patientRepository.count()).thenReturn(50L);
        lenient().when(pharmacistRepository.count()).thenReturn(5L);
        lenient().when(appointmentRepository.countByAppointmentDateTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(8L);
        lenient().when(appointmentRepository.countByStatus(AppointmentStatus.COMPLETED)).thenReturn(100L);
        lenient().when(prescriptionRepository.countByStatus(PrescriptionStatus.ACTIVE)).thenReturn(25L);
        lenient().when(medicalRecordRepository.count()).thenReturn(200L);
    }

    @Test
    void shouldGetDashboardStats() {
        // When
        DashboardStatsDTO stats = statisticsService.getDashboardStats();

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalDoctors()).isEqualTo(10L);
        assertThat(stats.getTotalPatients()).isEqualTo(50L);
        assertThat(stats.getTotalPharmacists()).isEqualTo(5L);
        assertThat(stats.getTodaysAppointments()).isEqualTo(8L);
        assertThat(stats.getCompletedAppointments()).isEqualTo(100L);
        assertThat(stats.getActivePrescriptions()).isEqualTo(25L);
        assertThat(stats.getTotalMedicalRecords()).isEqualTo(200L);
    }

    @Test
    void shouldReturnZeroWhenNoData() {
        // Given
        when(doctorRepository.count()).thenReturn(0L);
        when(patientRepository.count()).thenReturn(0L);
        when(pharmacistRepository.count()).thenReturn(0L);
        when(appointmentRepository.countByAppointmentDateTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(appointmentRepository.countByStatus(AppointmentStatus.COMPLETED)).thenReturn(0L);
        when(prescriptionRepository.countByStatus(PrescriptionStatus.ACTIVE)).thenReturn(0L);
        when(medicalRecordRepository.count()).thenReturn(0L);

        // When
        DashboardStatsDTO stats = statisticsService.getDashboardStats();

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalDoctors()).isEqualTo(0L);
        assertThat(stats.getTotalPatients()).isEqualTo(0L);
        assertThat(stats.getTotalPharmacists()).isEqualTo(0L);
        assertThat(stats.getTodaysAppointments()).isEqualTo(0L);
        assertThat(stats.getCompletedAppointments()).isEqualTo(0L);
        assertThat(stats.getActivePrescriptions()).isEqualTo(0L);
        assertThat(stats.getTotalMedicalRecords()).isEqualTo(0L);
    }

    @Test
    void shouldGetDoctorStats() {
        // Given
        Long doctorId = 1L;
        Doctor doctor = new Doctor();
        doctor.setId(doctorId);
        doctor.setFirstName("John");
        doctor.setLastName("Doe");

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(medicalRecordRepository.countDistinctPatientsByDoctorId(doctorId)).thenReturn(15L);
        when(appointmentRepository.countByDoctorId(doctorId)).thenReturn(50L);
        when(appointmentRepository.countByDoctorIdAndAppointmentDateTimeBetween(eq(doctorId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3L);
        when(appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.COMPLETED)).thenReturn(40L);
        when(medicalRecordRepository.countByDoctorId(doctorId)).thenReturn(30L);
        when(prescriptionRepository.countByDoctorId(doctorId)).thenReturn(25L);

        // When
        DoctorStatsDTO stats = statisticsService.getDoctorStats(doctorId);

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getDoctorId()).isEqualTo(doctorId);
        assertThat(stats.getDoctorName()).isEqualTo("John Doe");
        assertThat(stats.getTotalPatients()).isEqualTo(15L);
        assertThat(stats.getTotalAppointments()).isEqualTo(50L);
        assertThat(stats.getTodaysAppointments()).isEqualTo(3L);
        assertThat(stats.getCompletedAppointments()).isEqualTo(40L);
        assertThat(stats.getTotalMedicalRecords()).isEqualTo(30L);
        assertThat(stats.getTotalPrescriptions()).isEqualTo(25L);
    }

    @Test
    void shouldThrowExceptionWhenDoctorNotFound() {
        // Given
        Long doctorId = 999L;
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> statisticsService.getDoctorStats(doctorId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found with id: 999");
    }

    @Test
    void shouldGetDirectorDashboard() {
        // Given
        when(doctorRepository.count()).thenReturn(10L);
        when(patientRepository.count()).thenReturn(50L);
        when(pharmacistRepository.count()).thenReturn(5L);
        when(appointmentRepository.count()).thenReturn(100L);
        when(medicalRecordRepository.count()).thenReturn(200L);
        when(prescriptionRepository.count()).thenReturn(150L);
        when(appointmentRepository.countByStatus(AppointmentStatus.COMPLETED)).thenReturn(80L);
        when(appointmentRepository.countByStatus(AppointmentStatus.SCHEDULED)).thenReturn(15L);
        when(appointmentRepository.countByStatus(AppointmentStatus.CANCELLED)).thenReturn(5L);
        when(appointmentRepository.countByAppointmentDateTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(8L);
        when(appointmentRepository.countByStatusAndAppointmentDateTimeBetween(
                eq(AppointmentStatus.COMPLETED), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3L);
        when(appointmentRepository.countDistinctDoctorsByAppointmentDateTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(7L);
        when(medicalRecordRepository.countDistinctPatients()).thenReturn(45L);
        when(prescriptionRepository.countByStatus(PrescriptionStatus.ACTIVE)).thenReturn(25L);
        when(prescriptionRepository.countByStatus(PrescriptionStatus.DISPENSED)).thenReturn(100L);

        // When
        DirectorDashboardDTO dashboard = statisticsService.getDirectorDashboard();

        // Then
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getTotalDoctors()).isEqualTo(10L);
        assertThat(dashboard.getTotalPatients()).isEqualTo(50L);
        assertThat(dashboard.getTotalPharmacists()).isEqualTo(5L);
        assertThat(dashboard.getTotalAppointments()).isEqualTo(100L);
        assertThat(dashboard.getCompletedAppointments()).isEqualTo(80L);
        assertThat(dashboard.getAppointmentCompletionRate()).isEqualTo(80.0);
        assertThat(dashboard.getDoctorUtilizationRate()).isEqualTo(70.0);
        assertThat(dashboard.getAverageAppointmentsPerDoctor()).isEqualTo(10.0);
        assertThat(dashboard.getAveragePatientsPerDoctor()).isEqualTo(4.5);
    }

    @Test
    void shouldGetDoctorPerformance() {
        // Given
        Long doctorId = 1L;
        Doctor doctor = new Doctor();
        doctor.setId(doctorId);
        doctor.setFirstName("John");
        doctor.setLastName("Doe");
        doctor.setSpecialization("Cardiology");

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.countByDoctorId(doctorId)).thenReturn(50L);
        when(appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.COMPLETED)).thenReturn(40L);
        when(appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.CANCELLED)).thenReturn(5L);
        when(medicalRecordRepository.countDistinctPatientsByDoctorId(doctorId)).thenReturn(15L);
        when(medicalRecordRepository.countByDoctorId(doctorId)).thenReturn(30L);
        when(prescriptionRepository.countByDoctorId(doctorId)).thenReturn(25L);
        when(appointmentRepository.countByDoctorIdAndAppointmentDateTimeBetween(eq(doctorId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3L);

        // When
        DoctorPerformanceDTO performance = statisticsService.getDoctorPerformance(doctorId);

        // Then
        assertThat(performance).isNotNull();
        assertThat(performance.getDoctorId()).isEqualTo(doctorId);
        assertThat(performance.getDoctorName()).isEqualTo("John Doe");
        assertThat(performance.getSpecialization()).isEqualTo("Cardiology");
        assertThat(performance.getTotalAppointments()).isEqualTo(50L);
        assertThat(performance.getCompletedAppointments()).isEqualTo(40L);
        assertThat(performance.getCompletionRate()).isEqualTo(80.0);
    }
}
