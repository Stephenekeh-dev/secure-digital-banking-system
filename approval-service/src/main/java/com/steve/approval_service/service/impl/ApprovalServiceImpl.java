package com.steve.approval_service.service.impl;

import com.steve.approval_service.dto.ApprovalRequest;
import com.steve.approval_service.dto.ApprovalResponse;
import com.steve.approval_service.model.Approval;
import com.steve.approval_service.model.ApprovalStatus;
import com.steve.approval_service.repository.ApprovalRepository;
import com.steve.approval_service.service.ApprovalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

// FIX: Original was missing @Service — Spring never registered this bean
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    // Transactions above this threshold require manual review
    private static final BigDecimal HIGH_VALUE_THRESHOLD = BigDecimal.valueOf(10_000);

    private final ApprovalRepository approvalRepository;

    @Override
    @Transactional
    public ApprovalResponse createApproval(ApprovalRequest request) {
        // FIX: Original auto-approved everything — now applies basic business logic
        ApprovalStatus status;
        String reason;

        if (request.getAmount() != null && request.getAmount().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            status = ApprovalStatus.PENDING;
            reason = "High-value transaction requires manual review (amount > 10,000)";
            log.warn("High-value transaction flagged for review: transactionId={}, amount={}",
                    request.getTransactionId(), request.getAmount());
        } else {
            status = ApprovalStatus.APPROVED;
            reason = "Auto-approved: amount within standard limits";
        }

        Approval approval = Approval.builder()
                .transactionId(request.getTransactionId().toString())
                .status(status)
                .reason(reason)
                .build();

        Approval saved = approvalRepository.save(approval);
        log.info("Approval created: id={} status={}", saved.getId(), status);
        return mapToResponse(saved);
    }

    public ApprovalResponse updateApprovalStatus(Long approvalId,    // ← Long not UUID/String
                                                 ApprovalStatus status,
                                                 String reason) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Approval not found: " + approvalId));
        approval.setStatus(status);
        approval.setReason(reason);
        approvalRepository.save(approval);
        log.info("Approval {} updated to status: {}", approvalId, status);
        return mapToResponse(approval);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ApprovalResponse> getApprovalsByTransaction(String transactionId) { // ← String
        return approvalRepository.findByTransactionId(transactionId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalResponse> getApprovalsByStatus(ApprovalStatus status) {
        return approvalRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ApprovalResponse mapToResponse(Approval approval) {
        return ApprovalResponse.builder()
                .id(approval.getId())
                .transactionId(approval.getTransactionId().toString())
                .status(approval.getStatus())
                .reason(approval.getReason())
                .createdAt(approval.getCreatedAt())
                .updatedAt(approval.getUpdatedAt())
                .build();
    }
}