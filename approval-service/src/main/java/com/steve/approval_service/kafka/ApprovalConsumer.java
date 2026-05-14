package com.steve.approval_service.kafka;

import com.steve.approval_service.model.Approval;
import com.steve.approval_service.model.ApprovalStatus;
import com.steve.approval_service.model.TransactionType;
import com.steve.approval_service.repository.ApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalConsumer {

    private final ApprovalRepository approvalRepository;

    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("10000");

    @KafkaListener(
            topics = "approval-check",           // ← fixed topic name
            groupId = "approval-service-group"
    )
    public void processApproval(ApprovalEvent event) {
        log.info("Approval request received for transaction: {}, amount: {}",
                event.getTransactionId(), event.getAmount());

        // Avoid duplicate approvals
        if (approvalRepository.existsByTransactionId(event.getTransactionId())) {
            log.info("Approval already exists for: {}", event.getTransactionId());
            return;
        }

        ApprovalStatus status = event.getAmount()
                .compareTo(HIGH_VALUE_THRESHOLD) > 0
                ? ApprovalStatus.PENDING
                : ApprovalStatus.APPROVED;

        // Convert String transactionType to enum safely
        TransactionType transactionType;
        try {
            transactionType = TransactionType.valueOf(event.getTransactionType());
        } catch (Exception e) {
            transactionType = TransactionType.DEPOSIT; // safe default
            log.warn("Unknown transaction type: {}", event.getTransactionType());
        }

        Approval approval = Approval.builder()
                .transactionId(event.getTransactionId())
                .userEmail(event.getUserEmail())         // ← now populated
                .accountNumber(event.getAccountNumber()) // ← now populated
                .amount(event.getAmount())               // ← now populated
                .transactionType(transactionType)        // ← converted from String
                .status(status)
                .reason(status == ApprovalStatus.PENDING
                        ? "High-value transaction requires manual review"
                        : "Auto-approved: within standard limits")
                .build();

        approvalRepository.save(approval);
        log.info("✅ Approval saved with status: {} for transaction: {}",
                status, event.getTransactionId());
    }
}