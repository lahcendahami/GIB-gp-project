package com.example.notification.dto;

import com.example.notification.model.NotificationChannel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @Email(message = "Valid recipient email is required")
    @NotBlank(message = "Recipient email is required")
    private String recipientEmail;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Body is required")
    private String body;

    private NotificationChannel channel = NotificationChannel.EMAIL;
}
