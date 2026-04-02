package com.steve.approval_service.controller;

import com.steve.approval_service.dto.ApprovalRequest;
import com.steve.approval_service.dto.ApprovalResponse;
import com.steve.approval_service.model.ApprovalStatus;
import com.steve.approval_service.service.ApprovalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

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
            @PathVariable UUID id,
            @RequestParam ApprovalStatus status,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(approvalService.updateApprovalStatus(id, status, reason));
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<ApprovalResponse>> getApprovalsByTransaction(
            @PathVariable UUID transactionId) {
        return ResponseEntity.ok(approvalService.getApprovalsByTransaction(transactionId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ApprovalResponse>> getApprovalsByStatus(
            @PathVariable ApprovalStatus status) {
        return ResponseEntity.ok(approvalService.getApprovalsByStatus(status));
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