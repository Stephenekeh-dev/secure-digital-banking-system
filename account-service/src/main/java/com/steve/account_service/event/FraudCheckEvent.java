package com.steve.account_service.event;



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
    private String userId;          // ← holds the userEmail value
    private String accountNumber;
    private BigDecimal amount;
    private String transactionType; // plain String — "DEPOSIT" or "WITHDRAWAL"
}