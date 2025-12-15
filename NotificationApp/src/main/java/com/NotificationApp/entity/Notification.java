package com.NotificationApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Builder
@AllArgsConstructor
@Getter
@Setter
public class Notification {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "request_id", length = 255)
    private String requestId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Channel channel;

    @Column(length = 512, nullable = false)
    private String recipient;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String payload; // JSON string

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private NotificationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Notification() {}

    // builder-style convenience constructor (optional)
    public Notification(String id, String requestId, Channel channel, String recipient, String payload, NotificationStatus status) {
        this.id = id;
        this.requestId = requestId;
        this.channel = channel;
        this.recipient = recipient;
        this.payload = payload;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
