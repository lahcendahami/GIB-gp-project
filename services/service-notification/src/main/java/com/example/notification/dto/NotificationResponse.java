package com.example.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Stand-alone response wrapper for the Notification service.
 * Mirrors ApiResponse from shared-lib but lives here independently,
 * since this service intentionally does not depend on shared-lib.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse<T> {

    private boolean success;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> NotificationResponse<T> ok(T data) {
        return NotificationResponse.<T>builder()
                .success(true).message("OK").data(data).build();
    }

    public static <T> NotificationResponse<T> ok(String message, T data) {
        return NotificationResponse.<T>builder()
                .success(true).message(message).data(data).build();
    }

    public static <T> NotificationResponse<T> error(String message) {
        return NotificationResponse.<T>builder()
                .success(false).message(message).build();
    }
}
