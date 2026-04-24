package com.hospital.management.entities;

import com.hospital.management.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "administrators")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Administrator extends User {

    @Column(length = 100)
    private String department;

    @Column(length = 50)
    private String accessLevel;

    @PrePersist
    protected void onCreate() {
        if (getRole() == null) {
            setRole(UserRole.ADMIN);
        }
        if (getActive() == null) {
            setActive(true);
        }
    }
}
