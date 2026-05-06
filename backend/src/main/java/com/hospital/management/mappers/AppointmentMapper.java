package com.hospital.management.mappers;

import com.hospital.management.dto.AppointmentDTO;
import com.hospital.management.entities.Appointment;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "doctor.id", target = "doctorId")
    AppointmentDTO toDTO(Appointment appointment);

    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Appointment toEntity(AppointmentDTO dto);

    @AfterMapping
    default void enrichWithNames(@MappingTarget AppointmentDTO dto, Appointment appointment) {
        if (appointment.getPatient() != null) {
            dto.setPatientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName());
        }
        if (appointment.getDoctor() != null) {
            dto.setDoctorName("Dr. " + appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName());
        }
    }
}
