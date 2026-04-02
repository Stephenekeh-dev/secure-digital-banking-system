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

    @KafkaListener(topics = "transactions", groupId = "transaction-service")
    public void consume(TransactionEvent event) {
        log.info("Received transaction event: {} {} for account {}",
                event.getType(), event.getAmount(), event.getAccountNumber());

        TransactionType type;
        try {
            type = TransactionType.valueOf(event.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Unknown transaction type received: {}. Skipping.", event.getType());
            return; // Don't throw — bad event should not kill the consumer
        }

        Transaction tx = Transaction.builder()
                .accountNumber(event.getAccountNumber())
                .amount(event.getAmount())
                .type(type)
                .userEmail(event.getUserEmail())
                .targetAccount(event.getTargetAccount())
                .build();

        transactionRepository.save(tx);
        log.info("Transaction persisted: id={}", tx.getId());
    }
}