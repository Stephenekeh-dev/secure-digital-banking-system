package com.steve.notification_service.kafka;

import com.steve.notification_service.dto.NotificationRequest;
import com.steve.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// FIX: Original was missing @Component and @RequiredArgsConstructor
// FIX: notificationService field was never injected — NPE at runtime
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "notification-topic", groupId = "notification-group")
    public void consume(NotificationRequest request) {
        log.info("Received notification request for: {}", request.getRecipient());
        notificationService.sendNotification(request);
    }
}
