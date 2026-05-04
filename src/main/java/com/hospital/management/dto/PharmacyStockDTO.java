package com.hospital.management.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyStockDTO {

    private Long id;

    @NotNull(message = "Medication ID is required")
    private Long medicationId;

    private String medicationName;

    @NotNull(message = "Hospital ID is required")
    private Long hospitalId;

    private String hospitalName;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity;

    @NotNull(message = "Reorder level is required")
    @Min(value = 0, message = "Reorder level must be non-negative")
    private Integer reorderLevel;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    @NotBlank(message = "Batch number is required")
    @Size(max = 50, message = "Batch number must not exceed 50 characters")
    private String batchNumber;

    @NotNull(message = "Unit price is required")
    @Min(value = 0, message = "Unit price must be non-negative")
    private Double unitPrice;

    private LocalDateTime lastUpdated;

    private Boolean lowStock;

    private Boolean expiringSoon;
}
