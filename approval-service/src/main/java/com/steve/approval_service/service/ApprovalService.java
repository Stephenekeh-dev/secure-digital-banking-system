package com.steve.approval_service.service;

import com.steve.approval_service.dto.ApprovalRequest;
import com.steve.approval_service.dto.ApprovalResponse;
import com.steve.approval_service.model.ApprovalStatus;

import java.util.List;
import java.util.UUID;

public interface ApprovalService {
    ApprovalResponse createApproval(ApprovalRequest request);
    ApprovalResponse updateApprovalStatus(UUID approvalId, ApprovalStatus status, String reason);
    List<ApprovalResponse> getApprovalsByTransaction(UUID transactionId);
    List<ApprovalResponse> getApprovalsByStatus(ApprovalStatus status);
}
