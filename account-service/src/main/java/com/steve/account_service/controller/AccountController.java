package com.steve.account_service.controller;
import com.steve.account_service.dto.AccountResponse;
import com.steve.account_service.dto.CreateAccountRequest;
import com.steve.account_service.dto.UpdateBalanceRequest;
import com.steve.account_service.service.AccountService;
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
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;


    @PostMapping("/create")
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(request, auth.getName()));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable String accountNumber,
            Authentication auth) {
        return ResponseEntity.ok(accountService.getAccount(accountNumber, auth.getName()));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getUserAccounts(Authentication auth) {
        return ResponseEntity.ok(accountService.getAccountsByUser(auth.getName()));
    }

    @PostMapping("/update-balance")
    public ResponseEntity<AccountResponse> updateBalance(
            @Valid @RequestBody UpdateBalanceRequest request,
            Authentication auth) {
        return ResponseEntity.ok(accountService.updateBalance(request, auth.getName()));
    }

    // Testing Aiven Kafka connectivity





    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/test-kafka")
    public String testKafka() {
        // Send a simple string message
        kafkaTemplate.send("audit-events", "test-key",
                "Account service test message at " + Instant.now());
        return "Message sent to Kafka from Account Service!";
    }

    @GetMapping("/test-and-verify")
    public String testAndVerify() throws Exception {
        // Send a JSON object message
        Map<String, Object> event = new HashMap<>();
        event.put("service", "account-service");
        event.put("timestamp", Instant.now().toString());
        event.put("message", "Account service verification test");
        event.put("testId", System.currentTimeMillis());

        var future = kafkaTemplate.send("audit-events", "test-key", event);
        var result = future.get();

        return "Message confirmed from Account Service! Partition: " +
                result.getRecordMetadata().partition() +
                ", Offset: " + result.getRecordMetadata().offset();
    }

    // Exception Handlers
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}