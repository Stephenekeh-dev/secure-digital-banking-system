package com.steve.transaction_service.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
@Builder
@Data
public class CreateTransactionRequest {
    private String accountNumber;
    private BigDecimal amount;
    private String type;           // "DEPOSIT", "WITHDRAWAL", "TRANSFER"
    private String targetAccount;  // required only for TRANSFER
}