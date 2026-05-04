package com.hospital.management.mappers;

import com.hospital.management.dto.PatientDTO;
import com.hospital.management.entities.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(source = "hospital.id", target = "hospitalId")
    @Mapping(source = "hospital.name", target = "hospitalName")
    PatientDTO toDTO(Patient patient);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hospital", ignore = true)
    Patient toEntity(PatientDTO dto);
}
