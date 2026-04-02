package com.steve.audit_service.controller;

import com.steve.audit_service.model.AuditLog;
import com.steve.audit_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// FIX: Original was missing @RestController and @RequestMapping — endpoints were invisible
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogService auditLogService;

    @PostMapping("/log")
    public ResponseEntity<AuditLog> logEvent(@RequestBody AuditLog request) {
        AuditLog saved = auditLogService.recordEvent(
                request.getServiceName(),
                request.getAction(),
                request.getPerformedBy(),
                request.getDetails()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<AuditLog>> getLogsByUser(@PathVariable String userEmail) {
        return ResponseEntity.ok(auditLogService.getLogsByUser(userEmail));
    }

    @GetMapping("/service/{serviceName}")
    public ResponseEntity<List<AuditLog>> getLogsByService(@PathVariable String serviceName) {
        return ResponseEntity.ok(auditLogService.getLogsByService(serviceName));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleError(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
    }
}
