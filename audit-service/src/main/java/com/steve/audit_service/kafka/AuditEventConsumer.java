package com.steve.audit_service.kafka;

import com.steve.audit_service.dto.AuditEvent;
import com.steve.audit_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


import lombok.extern.slf4j.Slf4j;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// FIX: Original used System.out.println — replaced with proper @Slf4j logging
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final AuditLogService auditLogService;

    @KafkaListener(topics = "audit-events", groupId = "audit-service-group")
    public void consume(AuditEvent event) {
        log.info("Received audit event: [{}] {} by {}",
                event.getServiceName(), event.getAction(), event.getPerformedBy());

        auditLogService.recordEvent(
                event.getServiceName(),
                event.getAction(),
                event.getPerformedBy(),
                event.getDetails()
        );
    }
}
