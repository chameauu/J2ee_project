package com.hospital.management.mappers;

import com.hospital.management.dto.MedicalRecordDTO;
import com.hospital.management.entities.MedicalRecord;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MedicalRecordMapper {

    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(target = "doctorName", ignore = true)
    @Mapping(target = "doctorSpecialization", ignore = true)
    MedicalRecordDTO toDTO(MedicalRecord medicalRecord);

    @AfterMapping
    default void enrichDoctorInfo(MedicalRecord medicalRecord, @MappingTarget MedicalRecordDTO dto) {
        if (medicalRecord.getDoctor() != null) {
            dto.setDoctorName("Dr. " + medicalRecord.getDoctor().getFirstName() + " " + medicalRecord.getDoctor().getLastName());
            dto.setDoctorSpecialization(medicalRecord.getDoctor().getSpecialization());
        }
    }

    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "hospital", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MedicalRecord toEntity(MedicalRecordDTO dto);
}
