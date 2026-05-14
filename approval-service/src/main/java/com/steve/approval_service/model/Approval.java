package com.steve.approval_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "approvals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // or SEQUENCE/AUTO
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String accountNumber;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;


    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
 