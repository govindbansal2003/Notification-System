package com.status_service.entity;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class NotificationDto {
    private String channel;
    private String status;
}
