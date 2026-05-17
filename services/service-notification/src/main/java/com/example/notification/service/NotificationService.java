package com.example.notification.service;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.model.Notification;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public Notification findById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
    }

    public List<Notification> findByRecipient(Long recipientId) {
        return notificationRepository.findByRecipientId(recipientId);
    }

    public List<Notification> findPending() {
        return notificationRepository.findBySentFalse();
    }

    @Transactional
    public Notification send(NotificationRequest request) {
        Notification notification = Notification.builder()
                .recipientId(request.getRecipientId())
                .recipientEmail(request.getRecipientEmail())
                .subject(request.getSubject())
                .body(request.getBody())
                .channel(request.getChannel())
                .build();

        Notification saved = notificationRepository.save(notification);
        // Simulate dispatch inline
        dispatch(saved);
        return saved;
    }

    /**
     * Scheduled job: retry any unsent notifications every 60 seconds.
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void retryPending() {
        List<Notification> pending = notificationRepository.findBySentFalse();
        if (!pending.isEmpty()) {
            log.info("Retrying {} pending notification(s)…", pending.size());
            pending.forEach(this::dispatch);
        }
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private void dispatch(Notification n) {
        // In production: call an SMTP/SMS/Push gateway here.
        log.info("[{}] Sending notification id={} to {} — subject: {}",
                n.getChannel(), n.getId(), n.getRecipientEmail(), n.getSubject());
        n.setSent(true);
        n.setSentAt(LocalDateTime.now());
        notificationRepository.save(n);
    }
}
