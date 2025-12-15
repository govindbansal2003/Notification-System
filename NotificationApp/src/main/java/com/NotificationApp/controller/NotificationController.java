package com.NotificationApp.controller;

import com.NotificationApp.requests.NotifyRequest;
import com.NotificationApp.service.NotificationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
@Slf4j
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/notify")
    public ResponseEntity<Map<String,String>> notifyNow(@Valid @RequestBody NotifyRequest notifyRequest) {
        log.info("Notify request: {}", notifyRequest);
        String notificationId = notificationService.createAndPublish(notifyRequest);
        return ResponseEntity.ok(Map.of("notificationId", notificationId));
    }
}
