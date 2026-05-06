package com.hospital.management.mappers;

import com.hospital.management.dto.PrescriptionDTO;
import com.hospital.management.entities.Prescription;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PrescriptionMapper {

    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(source = "medicalRecord.id", target = "medicalRecordId")
    @Mapping(target = "patientName", ignore = true)
    @Mapping(target = "doctorName", ignore = true)
    @Mapping(target = "doctorSpecialization", ignore = true)
    PrescriptionDTO toDTO(Prescription prescription);

    @AfterMapping
    default void enrichWithNames(Prescription prescription, @MappingTarget PrescriptionDTO dto) {
        if (prescription.getPatient() != null) {
            dto.setPatientName(prescription.getPatient().getFirstName() + " " + prescription.getPatient().getLastName());
        }
        if (prescription.getDoctor() != null) {
            dto.setDoctorName("Dr. " + prescription.getDoctor().getFirstName() + " " + prescription.getDoctor().getLastName());
            dto.setDoctorSpecialization(prescription.getDoctor().getSpecialization());
        }
    }

    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "medicalRecord", ignore = true)
    @Mapping(target = "hospital", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Prescription toEntity(PrescriptionDTO dto);
}
