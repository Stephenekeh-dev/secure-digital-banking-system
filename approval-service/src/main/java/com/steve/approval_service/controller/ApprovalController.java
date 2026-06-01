package com.steve.approval_service.controller;

import com.steve.approval_service.dto.ApprovalRequest;
import com.steve.approval_service.dto.ApprovalResponse;
import com.steve.approval_service.model.ApprovalStatus;
import com.steve.approval_service.service.ApprovalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

// FIX: Original was missing @RestController and @RequestMapping — endpoints were never registered
@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping
    public ResponseEntity<ApprovalResponse> createApproval(
            @Valid @RequestBody ApprovalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(approvalService.createApproval(request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApprovalResponse> updateApprovalStatus(
            @PathVariable Long id,
            @RequestParam ApprovalStatus status,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(approvalService.updateApprovalStatus(id, status, reason));
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<ApprovalResponse>> getApprovalsByTransaction(
            @PathVariable String transactionId) {
        return ResponseEntity.ok(approvalService.getApprovalsByTransaction(transactionId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ApprovalResponse>> getApprovalsByStatus(
            @PathVariable ApprovalStatus status) {
        return ResponseEntity.ok(approvalService.getApprovalsByStatus(status));
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/test-kafka")
    public String testKafka() {
        kafkaTemplate.send("approval-check", "test-key",
                "Approval service test message at " + Instant.now());
        return "Message sent to Kafka from Approval Service!";
    }

    @GetMapping("/test-and-verify")
    public String testAndVerify() throws Exception {
        Map<String, Object> event = new HashMap<>();
        event.put("service", "approval-service");
        event.put("timestamp", Instant.now().toString());
        event.put("message", "Approval service verification test");
        event.put("testId", System.currentTimeMillis());

        var future = kafkaTemplate.send("approval-check", "test-key", event);
        var result = future.get();

        return "Message confirmed! Partition: " +
                result.getRecordMetadata().partition() +
                ", Offset: " + result.getRecordMetadata().offset();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}