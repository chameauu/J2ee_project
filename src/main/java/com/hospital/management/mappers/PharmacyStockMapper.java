package com.hospital.management.mappers;

import com.hospital.management.dto.PharmacyStockDTO;
import com.hospital.management.entities.PharmacyStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PharmacyStockMapper {
    
    @Mapping(source = "medication.id", target = "medicationId")
    @Mapping(source = "medication.name", target = "medicationName")
    @Mapping(target = "lowStock", expression = "java(pharmacyStock.isLowStock())")
    @Mapping(target = "expiringSoon", expression = "java(pharmacyStock.isExpiringSoon())")
    PharmacyStockDTO toDTO(PharmacyStock pharmacyStock);
    
    @Mapping(target = "medication", ignore = true)
    @Mapping(target = "lowStock", ignore = true)
    @Mapping(target = "expiringSoon", ignore = true)
    PharmacyStock toEntity(PharmacyStockDTO dto);
}
