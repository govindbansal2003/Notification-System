package com.status_service.entity;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@AllArgsConstructor
public class Notification {
    @Id
    private String id;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "channel")
    private String channel;

    @Column(name = "recipient")
    private String recipient;

    @Lob
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payloadJson; // store JSON text

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private NotificationStatus status;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    public Notification() {}

    public Notification(String id, String requestId, String channel, String recipient, String payloadJson, NotificationStatus status) {
        this.id = id;
        this.requestId = requestId;
        this.channel = channel;
        this.recipient = recipient;
        this.payloadJson = payloadJson;
        this.status = status;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
}

