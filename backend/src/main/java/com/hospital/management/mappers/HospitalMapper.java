package com.hospital.management.mappers;

import com.hospital.management.dto.HospitalDTO;
import com.hospital.management.entities.Hospital;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface HospitalMapper {

    HospitalDTO toDTO(Hospital hospital);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Hospital toEntity(HospitalDTO hospitalDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(HospitalDTO hospitalDTO, @MappingTarget Hospital hospital);
}
