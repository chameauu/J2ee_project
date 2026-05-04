package com.hospital.management.mappers;

import com.hospital.management.dto.PharmacistDTO;
import com.hospital.management.entities.Pharmacist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PharmacistMapper {

    @Mapping(source = "hospital.id", target = "hospitalId")
    @Mapping(source = "hospital.name", target = "hospitalName")
    PharmacistDTO toDTO(Pharmacist pharmacist);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hospital", ignore = true)
    Pharmacist toEntity(PharmacistDTO dto);
}
