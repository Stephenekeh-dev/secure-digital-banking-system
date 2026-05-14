package com.steve.transaction_service.event;

import com.steve.transaction_service.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEvent {
    private String transactionId;    // ← add this field
    private String accountNumber;
    private String userEmail;
    private BigDecimal amount;
    private String type;
    private String targetAccount;
}