package com.steve.transaction_service.controller;

import com.steve.transaction_service.dto.CreateTransactionRequest;
import com.steve.transaction_service.dto.TransactionResponse;
import com.steve.transaction_service.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody CreateTransactionRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(request, auth.getName()));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getUserTransactions(Authentication auth) {
        return ResponseEntity.ok(transactionService.getUserTransactions(auth.getName()));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransactionResponse>> getAccountTransactions(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionService.getAccountTransactions(accountNumber));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/test-kafka")
    public String testKafka() {
        kafkaTemplate.send("audit-events", "test-key",
                "Transaction service test message at " + Instant.now());
        return "Message sent to Kafka from Transaction Service!";
    }

    @GetMapping("/test-and-verify")
    public String testAndVerify() throws Exception {
        Map<String, Object> event = new HashMap<>();
        event.put("service", "transaction-service");
        event.put("timestamp", Instant.now().toString());
        event.put("message", "Transaction service verification test");
        event.put("testId", System.currentTimeMillis());

        var future = kafkaTemplate.send("audit-events", "test-key", event);
        var result = future.get();

        return "Message confirmed from Transaction Service! Partition: " +
                result.getRecordMetadata().partition() +
                ", Offset: " + result.getRecordMetadata().offset();
    }
}

