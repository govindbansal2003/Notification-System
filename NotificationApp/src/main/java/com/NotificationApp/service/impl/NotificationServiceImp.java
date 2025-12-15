package com.NotificationApp.service.impl;

import com.NotificationApp.entity.Channel;
import com.NotificationApp.entity.Notification;
import com.NotificationApp.entity.NotificationStatus;
import com.NotificationApp.requests.NotifyRequest;
import com.NotificationApp.repository.NotificationRepository;
import com.NotificationApp.utils.RedisService; // adjust to your package
import com.NotificationApp.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class NotificationServiceImp implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final RedisService redisService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String IDEMPOTENCY_PREFIX = "idemp:";
    private static final long IDEMPOTENCY_TTL = 24 * 3600; // seconds
    private static final String TOPIC = "notifications.rendered";
    private static final long RATE_LIMIT_TTL = 60; // seconds

    public NotificationServiceImp(NotificationRepository notificationRepository,
                                  RedisService redisService,
                                  ObjectMapper objectMapper,
                                  KafkaTemplate<String, String> kafkaTemplate) {
        this.notificationRepository = notificationRepository;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @Transactional
    public String createAndPublish(NotifyRequest notifyRequest) {
        String requestId = notifyRequest.getRequestId();
        String recipient = notifyRequest.getRecipient();

        String reservedId = null;
        if (requestId != null && !requestId.isBlank()) {
            String idemKey = IDEMPOTENCY_PREFIX + requestId;

            Object existing = redisService.get(idemKey);
            if (existing != null) {
                log.info("Idempotent hit for requestId={}, returning existing notification id {}", requestId, existing);
                return existing.toString();
            }

            reservedId = UUID.randomUUID().toString();
            boolean reserved = redisService.setIfAbsent(idemKey, reservedId, IDEMPOTENCY_TTL);
            if (!reserved) {
                Object val = redisService.get(idemKey);
                if (val != null) {
                    log.info("Idempotency key was set concurrently for requestId={}, returning {}", requestId, val);
                    return val.toString();
                }
            }
        }


        String rlKey = "rl:" + recipient;
        redisService.incrementWithTtl(rlKey, RATE_LIMIT_TTL);

        String id = reservedId != null ? reservedId : UUID.randomUUID().toString();


        Channel channelEnum = Channel.EMAIL;
        try {
            channelEnum = Channel.valueOf(notifyRequest.getChannel().toUpperCase());
        } catch (Exception ex) {
            log.warn("Unknown channel {}, defaulting to EMAIL", notifyRequest.getChannel());
        }

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(notifyRequest.getPayload());
        } catch (Exception e) {
            log.error("Failed to serialize payload, using empty object", e);
            payloadJson = "{}";
        }

        Notification n = new Notification(
                id,
                requestId,
                channelEnum,
                recipient,
                payloadJson,
                NotificationStatus.PENDING
        );
        notificationRepository.save(n);

        if (requestId != null && !requestId.isBlank()) {
            String idemKey = IDEMPOTENCY_PREFIX + requestId;
            redisService.setIfAbsent(idemKey, id, IDEMPOTENCY_TTL);
        }

        try {
            Map<String, Object> msg = Map.of(
                    "notificationId", id,
                    "channel", channelEnum.name(),
                    "recipient", recipient,
                    "payload",notifyRequest.getPayload()
            );
            String msgJson = objectMapper.writeValueAsString(msg);
            log.info("String message {}",msgJson);
            kafkaTemplate.send(TOPIC, id, msgJson);
            log.info("Published notification {} to topic {}", id, TOPIC);
        } catch (Exception e) {
            log.error("Failed to publish notification {} to Kafka", id, e);
        }

        return id;
    }
}
