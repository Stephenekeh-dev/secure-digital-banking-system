package com.steve.fraud_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheckEvent {
    private String transactionId;
    private String userId;          // ← matches account-service
    private String accountNumber;
    private BigDecimal amount;
    private String transactionType; // ← matches account-service
}