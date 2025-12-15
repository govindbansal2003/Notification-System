package com.NotificationApp.service;

import com.NotificationApp.requests.NotifyRequest;

public interface NotificationService {
    String createAndPublish(NotifyRequest notifyRequest);
}
