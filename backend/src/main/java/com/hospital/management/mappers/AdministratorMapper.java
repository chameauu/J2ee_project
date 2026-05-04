package com.hospital.management.mappers;

import com.hospital.management.dto.AdministratorDTO;
import com.hospital.management.entities.Administrator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdministratorMapper {

    @Mapping(source = "hospital.id", target = "hospitalId")
    @Mapping(source = "hospital.name", target = "hospitalName")
    AdministratorDTO toDTO(Administrator administrator);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hospital", ignore = true)
    Administrator toEntity(AdministratorDTO dto);
}
