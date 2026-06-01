package com.steve.fraud_service.kafka;

import com.steve.fraud_service.model.FraudActivity;
import com.steve.fraud_service.repository.FraudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class FraudConsumer {

    private final FraudRepository fraudActivityRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final BigDecimal FRAUD_THRESHOLD    = new BigDecimal("50000");
    private static final BigDecimal APPROVAL_THRESHOLD = new BigDecimal("10000");

    @KafkaListener(
            topics = "fraud-check",              // ← fixed topic name
            groupId = "fraud-service-group"
    )
    public void checkForFraud(FraudCheckEvent event) {
        log.info("Fraud check for transaction: {}, amount: {}",
                event.getTransactionId(), event.getAmount());

        // Flag as fraud if amount > 50,000
        if (event.getAmount().compareTo(FRAUD_THRESHOLD) > 0) {
            FraudActivity fraud = FraudActivity.builder()
                    .transactionId(event.getTransactionId())
                    .userId(event.getUserId())
                    .reason("High value transaction: NGN " + event.getAmount())
                    .amount(event.getAmount())
                    .flaggedAt(LocalDateTime.now())
                    .build();
            fraudActivityRepository.save(fraud);
            log.warn(" Fraud flagged for transaction: {}", event.getTransactionId());
        } else {
            log.info("Transaction {} passed fraud check", event.getTransactionId());
        }

        // Send to approval if amount > 10,000
        if (event.getAmount().compareTo(APPROVAL_THRESHOLD) > 0) {
            try {
                ApprovalEvent approvalEvent = ApprovalEvent.builder()
                        .transactionId(event.getTransactionId())
                        .userEmail(event.getUserId())    // userId holds the email
                        .accountNumber(event.getAccountNumber())
                        .amount(event.getAmount())
                        .transactionType(event.getTransactionType())
                        .status("PENDING")
                        .build();
                kafkaTemplate.send("approval-check", approvalEvent);
                log.info("Sent to approval: {}", event.getTransactionId());
            } catch (Exception e) {
                log.warn("Could not send to approval: {}", e.getMessage());
            }
        }
    }
}