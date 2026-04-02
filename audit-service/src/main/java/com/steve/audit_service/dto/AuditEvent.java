package com.steve.audit_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private String serviceName;
    private String action;
    private String performedBy;
    private String details;
    private LocalDateTime timestamp;
}