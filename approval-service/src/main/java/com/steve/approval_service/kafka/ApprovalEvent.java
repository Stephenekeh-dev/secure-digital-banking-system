package com.steve.approval_service.kafka;


import com.steve.approval_service.model.TransactionType;
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
    private String transactionType;  // plain String — matches fraud-service
    private String status;
}