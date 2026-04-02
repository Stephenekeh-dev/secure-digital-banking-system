package com.steve.notification_service.controller;

import com.steve.notification_service.dto.NotificationRequest;
import com.steve.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// FIX: Original was missing @RestController and @RequestMapping
// FIX: notificationService was never injected — NPE at runtime
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Map<String, String>> sendNotification(
            @Valid @RequestBody NotificationRequest request) {
        notificationService.sendNotification(request);
        return ResponseEntity.ok(Map.of("message", "Notification dispatched successfully"));
    }
}