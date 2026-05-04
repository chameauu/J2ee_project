package com.hospital.management.mappers;

import com.hospital.management.dto.PrescriptionDTO;
import com.hospital.management.entities.Prescription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PrescriptionMapper {

    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(source = "medicalRecord.id", target = "medicalRecordId")
    PrescriptionDTO toDTO(Prescription prescription);

    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "medicalRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Prescription toEntity(PrescriptionDTO dto);
}
