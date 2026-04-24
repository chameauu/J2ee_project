package com.hospital.management.repositories;

import com.hospital.management.entities.Appointment;
import com.hospital.management.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByAppointmentDateTimeDesc(Long patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateTimeAsc(Long doctorId);

    List<Appointment> findByDoctorIdAndStatusOrderByAppointmentDateTimeAsc(Long doctorId, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.appointmentDateTime BETWEEN :startTime AND :endTime " +
           "AND a.status != 'CANCELLED'")
    List<Appointment> findDoctorAppointmentsInTimeRange(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.status != 'CANCELLED' " +
           "AND ((a.appointmentDateTime <= :startTime AND " +
           "      FUNCTION('DATE_ADD', a.appointmentDateTime, a.durationMinutes, 'MINUTE') > :startTime) " +
           "OR (a.appointmentDateTime < :endTime AND " +
           "    FUNCTION('DATE_ADD', a.appointmentDateTime, a.durationMinutes, 'MINUTE') >= :endTime) " +
           "OR (a.appointmentDateTime >= :startTime AND " +
           "    FUNCTION('DATE_ADD', a.appointmentDateTime, a.durationMinutes, 'MINUTE') <= :endTime))")
    boolean hasTimeSlotConflict(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    Long countByAppointmentDateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    Long countByStatus(AppointmentStatus status);

    Long countByDoctorId(Long doctorId);

    Long countByDoctorIdAndAppointmentDateTimeBetween(Long doctorId, LocalDateTime startTime, LocalDateTime endTime);

    Long countByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
}
