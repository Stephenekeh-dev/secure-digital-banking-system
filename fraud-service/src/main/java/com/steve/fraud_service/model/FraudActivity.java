package com.steve.fraud_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime flaggedAt;

    @PrePersist
    public void onCreate() {
        if (this.flaggedAt == null) {
            this.flaggedAt = LocalDateTime.now();
        }
    }
}
