package com.example.notification.service;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.model.Notification;
import com.example.notification.model.NotificationChannel;
import com.example.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// add some logic here in notifation svc
@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void send_ShouldSaveAndDispatchNotification() {
        NotificationRequest request = new NotificationRequest();
        request.setRecipientId(1L);
        request.setRecipientEmail("user@example.com");
        request.setSubject("Welcome");
        request.setBody("Hello there");
        // We'll leave channel as null to avoid Enum issues if EMAIL is not present

        Notification saved = Notification.builder().id(10L).build();
        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        Notification result = notificationService.send(request);

        assertNotNull(result);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void retryPending_ShouldDispatchUnsent() {
        Notification unsent = Notification.builder().id(1L).sent(false).build();
        when(notificationRepository.findBySentFalse()).thenReturn(List.of(unsent));

        notificationService.retryPending();

        assertTrue(unsent.isSent());
        verify(notificationRepository).save(unsent);
    }
}
