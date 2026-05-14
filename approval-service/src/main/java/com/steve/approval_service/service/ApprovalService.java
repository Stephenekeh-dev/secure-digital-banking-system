package com.steve.approval_service.service;

import com.steve.approval_service.dto.ApprovalRequest;
import com.steve.approval_service.dto.ApprovalResponse;
import com.steve.approval_service.model.ApprovalStatus;

import java.util.List;
import java.util.UUID;

public interface ApprovalService {
    ApprovalResponse createApproval(ApprovalRequest request);
    ApprovalResponse updateApprovalStatus(Long approvalId,       // ← Long
                                          ApprovalStatus status,
                                          String reason);
    List<ApprovalResponse> getApprovalsByTransaction(String transactionId); // ← String
    List<ApprovalResponse> getApprovalsByStatus(ApprovalStatus status);
}