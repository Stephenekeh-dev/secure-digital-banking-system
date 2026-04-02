package com.steve.transaction_service.event;

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
    private String accountNumber;
    private BigDecimal amount;
    private String type;           // DEPOSIT | WITHDRAWAL | TRANSFER
    private String userEmail;
    private String targetAccount;
}