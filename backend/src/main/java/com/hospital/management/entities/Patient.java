package com.hospital.management.entities;

import com.hospital.management.enums.Gender;
import com.hospital.management.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "patients")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Patient extends User {

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(length = 10)
    private String bloodType;

    @Column(length = 255)
    private String address;

    private String emergencyContact;

    private String insuranceNumber;

    @PrePersist
    protected void onCreate() {
        if (getRole() == null) {
            setRole(UserRole.PATIENT);
        }
        if (getActive() == null) {
            setActive(true);
        }
    }
}
