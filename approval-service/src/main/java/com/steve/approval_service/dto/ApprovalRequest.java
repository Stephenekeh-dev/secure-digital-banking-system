package com.steve.approval_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRequest {
    private String transactionId;    // ← String not UUID
    private String userEmail;
    private String accountNumber;
    private BigDecimal amount;
    private String transactionType;
}