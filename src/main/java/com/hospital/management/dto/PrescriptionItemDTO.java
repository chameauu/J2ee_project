package com.hospital.management.dto;

import jakarta.validation.constraints.Min;
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
public class PrescriptionItemDTO {

    private Long id;

    @NotNull(message = "Prescription ID is required")
    private Long prescriptionId;

    @NotNull(message = "Medication ID is required")
    private Long medicationId;

    private String medicationName;

    @NotBlank(message = "Dosage is required")
    @Size(max = 100, message = "Dosage must not exceed 100 characters")
    private String dosage;

    @NotBlank(message = "Frequency is required")
    @Size(max = 100, message = "Frequency must not exceed 100 characters")
    private String frequency;

    @NotNull(message = "Duration in days is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationDays;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String instructions;

    private Boolean dispensed;

    private LocalDateTime dispensedAt;

    private Long dispensedByPharmacistId;

    private String dispensedByPharmacistName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
