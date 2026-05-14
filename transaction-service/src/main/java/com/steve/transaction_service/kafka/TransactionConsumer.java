package com.steve.transaction_service.kafka;

import com.steve.transaction_service.entity.Transaction;
import com.steve.transaction_service.entity.TransactionType;
import com.steve.transaction_service.event.TransactionEvent;
import com.steve.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionConsumer {

    private final TransactionRepository transactionRepository;

    @KafkaListener(
            topics = "transactions",
            groupId = "transaction-service-group"
    )
    public void consumeTransaction(TransactionEvent event) {
        try {
            log.info("Kafka event received: transactionId={}, account={}, type={}, amount={}",
                    event.getTransactionId(), event.getAccountNumber(),
                    event.getType(), event.getAmount());

            // Convert String to TransactionType enum safely
            TransactionType transactionType;
            try {
                transactionType = TransactionType.valueOf(event.getType().toUpperCase());
            } catch (Exception e) {
                log.warn("Unknown transaction type: {}, defaulting to DEPOSIT", event.getType());
                transactionType = TransactionType.DEPOSIT;
            }

            Transaction transaction = Transaction.builder()
                    .transactionId(event.getTransactionId())
                    .accountNumber(event.getAccountNumber())
                    .userEmail(event.getUserEmail())
                    .amount(event.getAmount())
                    .type(transactionType)              // ← now passing enum
                    .targetAccount(event.getTargetAccount())
                    .createdAt(LocalDateTime.now())
                    .build();

            transactionRepository.save(transaction);
            log.info("Transaction saved: {}", event.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to process transaction: {}", e.getMessage(), e);
        }
    }
}