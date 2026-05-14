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
public class ApprovalEvent {
    private String transactionId;
    private String userEmail;
    private String accountNumber;
    private BigDecimal amount;
    private String transactionType;
}