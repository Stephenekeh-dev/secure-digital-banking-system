package com.steve.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    @NotBlank(message = "Recipient is required")
    private String recipient;   // email address or phone number

    @NotBlank(message = "Message is required")
    private String message;

    private String type;        // EMAIL | SMS — defaults to EMAIL if not specified
}
