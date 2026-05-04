package com.hospital.management.mappers;

import com.hospital.management.dto.MedicationDTO;
import com.hospital.management.entities.Medication;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MedicationMapper {
    MedicationDTO toDTO(Medication medication);
    Medication toEntity(MedicationDTO dto);
}
