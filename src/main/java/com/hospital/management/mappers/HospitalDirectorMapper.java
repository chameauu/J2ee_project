package com.hospital.management.mappers;

import com.hospital.management.dto.HospitalDirectorDTO;
import com.hospital.management.entities.HospitalDirector;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HospitalDirectorMapper {
    HospitalDirectorDTO toDTO(HospitalDirector hospitalDirector);
    HospitalDirector toEntity(HospitalDirectorDTO dto);
}
