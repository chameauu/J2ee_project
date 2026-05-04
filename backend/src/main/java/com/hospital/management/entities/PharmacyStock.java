package com.hospital.management.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pharmacy_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PharmacyStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer reorderLevel;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false, length = 50)
    private String batchNumber;

    @Column(nullable = false)
    private Double unitPrice;

    @LastModifiedDate
    private LocalDateTime lastUpdated;

    // Business logic methods
    public boolean isLowStock() {
        return quantity <= reorderLevel;
    }

    public boolean isExpiringSoon() {
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return expiryDate.isBefore(thirtyDaysFromNow) || expiryDate.isEqual(thirtyDaysFromNow);
    }

    public void reduceStock(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (quantity < amount) {
            throw new IllegalStateException("Insufficient stock. Available: " + quantity + ", Requested: " + amount);
        }
        this.quantity -= amount;
    }

    public void addStock(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.quantity += amount;
    }
}
