package com.steve.notification_service;



import com.steve.notification_service.dto.NotificationRequest;
import com.steve.notification_service.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock private JavaMailSender mailSender;
    @InjectMocks private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendNotification_email_callsMailSender() {
        NotificationRequest request = NotificationRequest.builder()
                .recipient("user@bank.com")
                .message("Your deposit of $500 was successful.")
                .type("EMAIL")
                .build();

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> notificationService.sendNotification(request))
                .doesNotThrowAnyException();

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotification_defaultsToEmail_whenTypeNull() {
        NotificationRequest request = NotificationRequest.builder()
                .recipient("user@bank.com")
                .message("Account created.")
                .build(); // no type set

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        notificationService.sendNotification(request);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotification_sms_doesNotCallMailSender() {
        NotificationRequest request = NotificationRequest.builder()
                .recipient("+2348012345678")
                .message("Your OTP is 123456.")
                .type("SMS")
                .build();

        notificationService.sendNotification(request);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotification_doesNotThrow_whenMailSenderFails() {
        NotificationRequest request = NotificationRequest.builder()
                .recipient("user@bank.com")
                .message("Test message")
                .type("EMAIL")
                .build();

        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        // Should swallow the exception and log — not crash the consumer
        assertThatCode(() -> notificationService.sendNotification(request))
                .doesNotThrowAnyException();
    }
}