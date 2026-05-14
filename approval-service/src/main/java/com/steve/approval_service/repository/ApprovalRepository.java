package com.steve.approval_service.repository;

import com.steve.approval_service.model.Approval;
import com.steve.approval_service.model.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findByStatus(ApprovalStatus status);
    List<Approval> findByUserEmail(String userEmail);
    List<Approval> findByTransactionId(String transactionId);
    boolean existsByTransactionId(String transactionId);  // ← add this
}