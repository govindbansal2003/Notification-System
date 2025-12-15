package com.notification_worker.service;

import com.notification_worker.entity.Notification;
import com.notification_worker.entity.NotificationStatus;
import com.notification_worker.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class NotificationListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    private final EmailSender emailSender;
    private final TemplateRenderer templateRenderer;

    public NotificationListener(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            NotificationRepository notificationRepository,
            EmailSender emailSender,
            TemplateRenderer templateRenderer) {

        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.notificationRepository = notificationRepository;
        this.emailSender = emailSender;
        this.templateRenderer = templateRenderer;
    }

    @KafkaListener(topics = "notifications.rendered", groupId = "worker-group")
    public void handleRendered(String message) {
        log.info("Worker received message = {}", message);

        try {
            Map<String, Object> input =
                    objectMapper.readValue(message, Map.class);

            String id = (String) input.get("notificationId");
            String channel = (String) input.get("channel");
            String recipient = (String) input.get("recipient");
            String payload = (String)input.get("payload");


            if (id == null || channel == null || recipient == null) {
                log.error("Invalid message received: {}", message);
                return;
            }


            if ("EMAIL".equalsIgnoreCase(channel)) {
                emailSender.send(
                        recipient,
                        "Welcome",
                        payload
                );
            } else {
                log.warn("Unsupported channel: {}", channel);
            }


            updateDbStatus(id, NotificationStatus.SENT);
            publishStatus(id, NotificationStatus.SENT);

        } catch (Exception ex) {
            log.error("Worker failed to process message", ex);
        }
    }

    @Transactional
    protected void updateDbStatus(String id, NotificationStatus status) {
        Optional<Notification> op = notificationRepository.findById(id);

        if (op.isEmpty()) {
            log.warn("Notification {} not found in DB", id);
            return;
        }

        Notification n = op.get();
        n.setStatus(status);
        n.setUpdatedAt(Instant.now());
        notificationRepository.save(n);

        log.info("DB updated : {} -> {}", id, status);
    }

    protected void publishStatus(String id, NotificationStatus status) {
        try {
            Map<String, Object> out = new HashMap<>();
            out.put("notificationId", id);
            out.put("status", status.name());
            out.put("timestamp", Instant.now().toString());

            String json = objectMapper.writeValueAsString(out);

            kafkaTemplate.send("notifications.status", id, json);

            log.info("Status message published: {}", json);

        } catch (Exception e) {
            log.error("Failed to publish status for {}", id, e);
        }
    }
}
