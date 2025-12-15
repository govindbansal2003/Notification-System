package com.status_service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.status_service.entity.Notification;
import com.status_service.entity.NotificationDto;
import com.status_service.respository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
@Slf4j
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDto> getById(@PathVariable String id) {
        Optional<Notification> opt = notificationRepository.findById(id);
        NotificationDto dto = null;
        if(opt.isPresent()){
            Notification n = opt.get();
            dto = NotificationDto.builder().
                    channel(n.getChannel()).
                    status(String.valueOf(n.getStatus())).
                    build();
        }

        return ResponseEntity.ok().body(dto);
    }
}
