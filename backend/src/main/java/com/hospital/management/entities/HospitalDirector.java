package com.hospital.management.entities;

import com.hospital.management.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "hospital_directors")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class HospitalDirector extends User {

    @Column(length = 100)
    private String hospitalName;

    private LocalDate appointmentDate;

    @Column(length = 500)
    private String credentials;

    @PrePersist
    protected void onCreate() {
        if (getRole() == null) {
            setRole(UserRole.DIRECTOR);
        }
        if (getActive() == null) {
            setActive(true);
        }
    }
}
