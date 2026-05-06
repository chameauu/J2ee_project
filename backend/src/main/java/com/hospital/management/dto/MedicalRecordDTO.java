package com.hospital.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordDTO {

    private Long id;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    // Read-only fields for doctor information
    private String doctorName;
    private String doctorSpecialization;

    private LocalDateTime visitDate;

    @NotBlank(message = "Chief complaint is required")
    private String chiefComplaint;

    @NotBlank(message = "Diagnosis is required")
    private String diagnosis;

    @NotBlank(message = "Treatment is required")
    private String treatment;

    private String notes;

    private String vitalSigns;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
