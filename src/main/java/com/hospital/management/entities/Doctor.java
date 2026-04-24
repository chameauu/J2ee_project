package com.hospital.management.entities;

import com.hospital.management.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doctors")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Doctor extends User {

    @Column(nullable = false, length = 100)
    private String specialization;

    @Column(nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Column(nullable = false)
    private Integer yearsOfExperience;

    @Column(length = 200)
    private String qualification;

    @PrePersist
    protected void onCreate() {
        if (getRole() == null) {
            setRole(UserRole.DOCTOR);
        }
        if (getActive() == null) {
            setActive(true);
        }
    }
}
