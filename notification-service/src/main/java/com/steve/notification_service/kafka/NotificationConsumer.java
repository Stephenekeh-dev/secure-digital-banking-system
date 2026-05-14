package com.steve.notification_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final JavaMailSender mailSender;

    @KafkaListener(
            topics = "notification-topic",
            groupId = "notification-group"
    )
    public void handleNotification(NotificationEvent event) {
        log.info("Notification received for: {}", event.getEmail());
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(event.getEmail());
            message.setSubject(event.getSubject());
            message.setText(event.getMessage());
            message.setFrom("ekehsteven2@gmail.com");
            mailSender.send(message);
            log.info("Email sent successfully to: {}", event.getEmail());
        } catch (Exception e) {
            log.error(" Failed to send email to {}: {}", event.getEmail(), e.getMessage());
        }
    }
}
