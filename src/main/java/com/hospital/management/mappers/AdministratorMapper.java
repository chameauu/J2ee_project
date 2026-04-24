package com.hospital.management.mappers;

import com.hospital.management.dto.AdministratorDTO;
import com.hospital.management.entities.Administrator;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdministratorMapper {

    AdministratorDTO toDTO(Administrator administrator);

    Administrator toEntity(AdministratorDTO dto);
}
