package com.status_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.status_service.entity.Notification;
import com.status_service.entity.NotificationStatus;
import com.status_service.respository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class StatusListener {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public StatusListener(NotificationRepository notificationRepository, ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "notifications.status", groupId = "status-service-group")
    public void onMessage(String message) {
        log.info("Received status message: {}", message);

        try {
            Map<String, Object> payload = objectMapper.readValue(message, new TypeReference<Map<String, Object>>(){});
            String notificationId = (String) payload.get("notificationId");
            if (notificationId == null) {
                notificationId = (String) payload.get("id");
            }

            if (notificationId == null) {
                log.warn("Message missing notificationId, ignoring");
                return;
            }

            String statusStr = (String) payload.get("status");

            NotificationStatus newStatus = NotificationStatus.PENDING;
            if (statusStr != null) {
                try { newStatus = NotificationStatus.valueOf(statusStr.toUpperCase()); }
                catch (Exception ex) { log.warn("Unknown status {}. defaulting to PENDING", statusStr); }
            }

            Optional<Notification> opt = notificationRepository.findById(notificationId);
            if (opt.isPresent()) {
                Notification n = opt.get();
                n.setStatus(newStatus);
                notificationRepository.save(n);
                log.info("Updated notification {} -> {}", notificationId, newStatus);
            } else {
                Notification n = new Notification(notificationId, null, null, null, null, newStatus);
                notificationRepository.save(n);
                log.info("Created minimal notification record {} with status {}", notificationId, newStatus);
            }
        } catch (Exception e) {
            log.error("Failed to process status message", e);
        }
    }
}
