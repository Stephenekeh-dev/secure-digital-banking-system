package com.steve.audit_service.service;

import com.steve.audit_service.model.AuditLog;
import com.steve.audit_service.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// FIX: Original was missing @Service — Spring would never register this bean
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;

    @Transactional
    public AuditLog recordEvent(String serviceName, String action,
                                String performedBy, String details) {
        AuditLog auditLog = AuditLog.builder()
                .serviceName(serviceName)
                .action(action)
                .performedBy(performedBy)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        AuditLog saved = repository.save(auditLog);
        log.info("Audit recorded: [{}] {} by {}", serviceName, action, performedBy);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAllLogs() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByUser(String userEmail) {
        return repository.findByPerformedByOrderByTimestampDesc(userEmail);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByService(String serviceName) {
        return repository.findByServiceNameOrderByTimestampDesc(serviceName);
    }
}