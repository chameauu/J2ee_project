package com.hospital.management.mappers;

import com.hospital.management.dto.HospitalDirectorDTO;
import com.hospital.management.entities.HospitalDirector;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HospitalDirectorMapper {

    @Mapping(source = "hospital.id", target = "hospitalId")
    // Note: hospitalName is a direct field in HospitalDirector entity, not from relationship
    HospitalDirectorDTO toDTO(HospitalDirector hospitalDirector);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hospital", ignore = true)
    HospitalDirector toEntity(HospitalDirectorDTO dto);
}
