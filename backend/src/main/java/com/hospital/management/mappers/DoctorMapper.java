package com.hospital.management.mappers;

import com.hospital.management.dto.DoctorDTO;
import com.hospital.management.entities.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(source = "hospital.id", target = "hospitalId")
    @Mapping(source = "hospital.name", target = "hospitalName")
    DoctorDTO toDTO(Doctor doctor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hospital", ignore = true)
    Doctor toEntity(DoctorDTO dto);
}
