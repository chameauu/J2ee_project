package com.hospital.management.entities;

import com.hospital.management.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pharmacists")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Pharmacist extends User {

    @Column(nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Column(length = 200)
    private String qualification;

    @PrePersist
    protected void onCreate() {
        if (getRole() == null) {
            setRole(UserRole.PHARMACIST);
        }
        if (getActive() == null) {
            setActive(true);
        }
    }
}
