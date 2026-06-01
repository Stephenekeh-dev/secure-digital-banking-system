package com.steve.fraud_service.controller;

import com.steve.fraud_service.model.FraudActivity;
import com.steve.fraud_service.service.FraudService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

// FIX: Original used @Autowired constructor — replaced with @RequiredArgsConstructor
@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudService fraudService;

    @PostMapping
    public ResponseEntity<FraudActivity> flagFraudActivity(
            @Valid @RequestBody FraudActivity request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fraudService.flagActivity(request));
    }

    @GetMapping
    public ResponseEntity<List<FraudActivity>> getAllFraudActivities() {
        return ResponseEntity.ok(fraudService.getAllFraudActivities());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FraudActivity>> getFraudByUser(@PathVariable String userId) {
        return ResponseEntity.ok(fraudService.getFraudActivitiesByUser(userId));
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<FraudActivity>> getFraudByTransaction(
            @PathVariable String transactionId) {
        return ResponseEntity.ok(fraudService.getFraudActivitiesByTransaction(transactionId));
    }

    @GetMapping("/above-amount/{threshold}")
    public ResponseEntity<List<FraudActivity>> getFraudAboveAmount(
            @PathVariable BigDecimal threshold) {
        return ResponseEntity.ok(fraudService.getFraudActivitiesAboveAmount(threshold));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFraudActivity(@PathVariable Long id) {
        fraudService.deleteFraudActivity(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
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
                "Fraud service test message at " + Instant.now());
        return "Message sent to Kafka from Fraud Service!";
    }

    @GetMapping("/test-and-verify")
    public String testAndVerify() throws Exception {
        Map<String, Object> event = new HashMap<>();
        event.put("service", "fraud-service");
        event.put("timestamp", Instant.now().toString());
        event.put("message", "Fraud service verification test");
        event.put("testId", System.currentTimeMillis());

        var future = kafkaTemplate.send("audit-events", "test-key", event);
        var result = future.get();

        return "Message confirmed from Fraud Service! Partition: " +
                result.getRecordMetadata().partition() +
                ", Offset: " + result.getRecordMetadata().offset();
    }
}
