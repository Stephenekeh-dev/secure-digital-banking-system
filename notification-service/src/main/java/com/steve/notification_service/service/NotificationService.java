package com.steve.notification_service.service;

import com.steve.notification_service.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// FIX: Original was missing @Service — Spring never registered this bean
// FIX: Original used `log` without declaring it (no @Slf4j) — would not compile
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    public void sendNotification(NotificationRequest request) {
        String type = request.getType() != null ? request.getType().toUpperCase() : "EMAIL";

        switch (type) {
            case "EMAIL" -> sendEmail(request);
            case "SMS"   -> sendSms(request);
            default      -> {
                log.warn("Unknown notification type '{}', defaulting to EMAIL", type);
                sendEmail(request);
            }
        }
    }

    private void sendEmail(NotificationRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getRecipient());
            message.setSubject("Banking System Notification");
            message.setText(request.getMessage());
            mailSender.send(message);
            log.info("Email sent to: {}", request.getRecipient());
        } catch (Exception e) {
            // Don't let notification failure crash the service — log and continue
            log.error("Failed to send email to {}: {}", request.getRecipient(), e.getMessage());
        }
    }

    private void sendSms(NotificationRequest request) {
        // Placeholder: integrate with Twilio or AWS SNS here
        log.info("SMS [STUB] to {}: {}", request.getRecipient(), request.getMessage());
    }
}