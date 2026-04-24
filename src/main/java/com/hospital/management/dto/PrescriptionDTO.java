package com.hospital.management.dto;

import com.hospital.management.enums.PrescriptionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDTO {

    private Long id;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    private Long medicalRecordId;

    @NotNull(message = "Prescribed date is required")
    private LocalDateTime prescribedDate;

    @NotNull(message = "Valid until date is required")
    private LocalDate validUntil;

    @NotNull(message = "Status is required")
    private PrescriptionStatus status;

    @NotNull(message = "Medication name is required")
    private String medicationName;

    @NotNull(message = "Dosage is required")
    private String dosage;

    @NotNull(message = "Frequency is required")
    private String frequency;

    private Integer durationDays;

    private String instructions;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
