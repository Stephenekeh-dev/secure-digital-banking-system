package com.steve.audit_service;

import com.steve.audit_service.model.AuditLog;
import com.steve.audit_service.repository.AuditLogRepository;
import com.steve.audit_service.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditLogServiceTest {

    @Mock private AuditLogRepository repository;
    @InjectMocks private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void recordEvent_savesAndReturnsLog() {
        AuditLog saved = AuditLog.builder()
                .id(1L)
                .serviceName("auth-service")
                .action("LOGIN_SUCCESS")
                .performedBy("user@bank.com")
                .details("User login")
                .build();

        when(repository.save(any())).thenReturn(saved);

        AuditLog result = auditLogService.recordEvent(
                "auth-service", "LOGIN_SUCCESS", "user@bank.com", "User login");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAction()).isEqualTo("LOGIN_SUCCESS");
        assertThat(result.getServiceName()).isEqualTo("auth-service");
        verify(repository, times(1)).save(any());
    }

    @Test
    void getLogsByUser_delegatesToRepository() {
        List<AuditLog> logs = List.of(
                AuditLog.builder().performedBy("user@bank.com").action("LOGIN_SUCCESS").build(),
                AuditLog.builder().performedBy("user@bank.com").action("DEPOSIT").build()
        );
        when(repository.findByPerformedByOrderByTimestampDesc("user@bank.com")).thenReturn(logs);

        List<AuditLog> result = auditLogService.getLogsByUser("user@bank.com");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAction()).isEqualTo("LOGIN_SUCCESS");
    }

    @Test
    void getLogsByService_delegatesToRepository() {
        List<AuditLog> logs = List.of(
                AuditLog.builder().serviceName("account-service").action("DEPOSIT").build()
        );
        when(repository.findByServiceNameOrderByTimestampDesc("account-service")).thenReturn(logs);

        List<AuditLog> result = auditLogService.getLogsByService("account-service");

        assertThat(result).hasSize(1);
    }
}