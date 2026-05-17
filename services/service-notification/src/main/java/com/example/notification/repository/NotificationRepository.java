package com.example.notification.repository;

import com.example.notification.model.Notification;
import com.example.notification.model.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientId(Long recipientId);
    List<Notification> findBySentFalse();
    List<Notification> findByChannel(NotificationChannel channel);
}
