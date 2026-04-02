package com.steve.approval_service;

import com.steve.approval_service.dto.ApprovalRequest;
import com.steve.approval_service.dto.ApprovalResponse;
import com.steve.approval_service.model.Approval;
import com.steve.approval_service.model.ApprovalStatus;
import com.steve.approval_service.repository.ApprovalRepository;
import com.steve.approval_service.service.impl.ApprovalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApprovalServiceTest {

    @Mock private ApprovalRepository approvalRepository;
    @InjectMocks private ApprovalServiceImpl approvalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createApproval_autoApproves_whenAmountBelowThreshold() {
        UUID txId = UUID.randomUUID();
        ApprovalRequest request = ApprovalRequest.builder()
                .transactionId(txId)
                .amount(BigDecimal.valueOf(500))
                .build();

        when(approvalRepository.save(any())).thenAnswer(inv -> {
            Approval a = inv.getArgument(0);
            a = Approval.builder()
                    .id(UUID.randomUUID())
                    .transactionId(a.getTransactionId())
                    .status(a.getStatus())
                    .reason(a.getReason())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return a;
        });

        ApprovalResponse response = approvalService.createApproval(request);

        assertThat(response.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(response.getReason()).containsIgnoringCase("auto-approved");
    }

    @Test
    void createApproval_setsPending_whenAmountAboveThreshold() {
        UUID txId = UUID.randomUUID();
        ApprovalRequest request = ApprovalRequest.builder()
                .transactionId(txId)
                .amount(BigDecimal.valueOf(15_000))
                .build();

        when(approvalRepository.save(any())).thenAnswer(inv -> {
            Approval a = inv.getArgument(0);
            return Approval.builder()
                    .id(UUID.randomUUID())
                    .transactionId(a.getTransactionId())
                    .status(a.getStatus())
                    .reason(a.getReason())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        });

        ApprovalResponse response = approvalService.createApproval(request);

        assertThat(response.getStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(response.getReason()).containsIgnoringCase("manual review");
    }

    @Test
    void updateApprovalStatus_updatesSuccessfully() {
        UUID approvalId = UUID.randomUUID();
        Approval existing = Approval.builder()
                .id(approvalId)
                .transactionId(UUID.randomUUID())
                .status(ApprovalStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(approvalRepository.findById(approvalId)).thenReturn(Optional.of(existing));
        when(approvalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApprovalResponse response = approvalService.updateApprovalStatus(
                approvalId, ApprovalStatus.APPROVED, "Manually reviewed and approved");

        assertThat(response.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(response.getReason()).isEqualTo("Manually reviewed and approved");
    }

    @Test
    void updateApprovalStatus_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(approvalRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                approvalService.updateApprovalStatus(id, ApprovalStatus.REJECTED, "Fraud suspected"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Approval not found");
    }

    @Test
    void getApprovalsByTransaction_returnsMatchingList() {
        UUID txId = UUID.randomUUID();
        List<Approval> approvals = List.of(
                Approval.builder().id(UUID.randomUUID()).transactionId(txId)
                        .status(ApprovalStatus.APPROVED).createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now()).build()
        );
        when(approvalRepository.findByTransactionId(txId)).thenReturn(approvals);

        List<ApprovalResponse> result = approvalService.getApprovalsByTransaction(txId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ApprovalStatus.APPROVED);
    }

    @Test
    void getApprovalsByStatus_returnsMatchingList() {
        List<Approval> pending = List.of(
                Approval.builder().id(UUID.randomUUID()).transactionId(UUID.randomUUID())
                        .status(ApprovalStatus.PENDING).createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now()).build(),
                Approval.builder().id(UUID.randomUUID()).transactionId(UUID.randomUUID())
                        .status(ApprovalStatus.PENDING).createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now()).build()
        );
        when(approvalRepository.findByStatus(ApprovalStatus.PENDING)).thenReturn(pending);

        List<ApprovalResponse> result = approvalService.getApprovalsByStatus(ApprovalStatus.PENDING);

        assertThat(result).hasSize(2);
        result.forEach(r -> assertThat(r.getStatus()).isEqualTo(ApprovalStatus.PENDING));
    }
}

