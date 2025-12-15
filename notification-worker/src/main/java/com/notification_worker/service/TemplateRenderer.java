package com.notification_worker.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TemplateRenderer {

    public String render(String templateId, Map<String, Object> payload) {

        if ("welcome_v1".equals(templateId)) {
            return "Hello " + payload.get("name")
                    + ",\nWelcome to our application 🎉";
        }

        throw new IllegalArgumentException("Unknown templateId: " + templateId);
    }
}
