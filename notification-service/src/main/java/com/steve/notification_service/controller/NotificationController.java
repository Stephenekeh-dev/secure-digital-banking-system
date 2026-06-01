package com.steve.notification_service.controller;

import com.steve.notification_service.dto.NotificationRequest;
import com.steve.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
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

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/test-kafka")
    public String testKafka() {
        kafkaTemplate.send("audit-events", "test-key",
                "Notification service test message at " + Instant.now());
        return "Message sent to Kafka from Notification Service!";
    }

    @GetMapping("/test-and-verify")
    public String testAndVerify() throws Exception {
        Map<String, Object> event = new HashMap<>();
        event.put("service", "notification-service");
        event.put("timestamp", Instant.now().toString());
        event.put("message", "Notification service verification test");
        event.put("testId", System.currentTimeMillis());

        var future = kafkaTemplate.send("audit-events", "test-key", event);
        var result = future.get();

        return "Message confirmed from Notification Service! Partition: " +
                result.getRecordMetadata().partition() +
                ", Offset: " + result.getRecordMetadata().offset();
    }
}