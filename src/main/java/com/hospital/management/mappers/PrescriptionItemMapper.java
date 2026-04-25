package com.hospital.management.mappers;

import com.hospital.management.dto.PrescriptionItemDTO;
import com.hospital.management.entities.PrescriptionItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PrescriptionItemMapper {

    @Mapping(source = "prescription.id", target = "prescriptionId")
    @Mapping(source = "medication.id", target = "medicationId")
    @Mapping(source = "medication.name", target = "medicationName")
    @Mapping(source = "dispensedBy.id", target = "dispensedByPharmacistId")
    @Mapping(source = "dispensedBy.firstName", target = "dispensedByPharmacistName")
    PrescriptionItemDTO toDTO(PrescriptionItem prescriptionItem);

    @Mapping(target = "prescription", ignore = true)
    @Mapping(target = "medication", ignore = true)
    @Mapping(target = "dispensedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PrescriptionItem toEntity(PrescriptionItemDTO dto);
}
