package com.hospital.management.dto;

import com.hospital.management.enums.MedicationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationDTO {

    private Long id;

    @NotBlank(message = "Medication name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 200, message = "Generic name must not exceed 200 characters")
    private String genericName;

    @Size(max = 100, message = "Manufacturer must not exceed 100 characters")
    private String manufacturer;

    @NotNull(message = "Medication type is required")
    private MedicationType type;

    @Size(max = 50, message = "Strength must not exceed 50 characters")
    private String strength;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
