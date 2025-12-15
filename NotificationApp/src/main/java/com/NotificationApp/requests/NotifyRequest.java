package com.NotificationApp.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Builder
@Jacksonized
@Getter
@Setter
public class NotifyRequest {
    @NotBlank
    private String requestId;

    @NotBlank
    private String channel;

    @NotBlank
    private String recipient;

    @NotNull
    private String payload;
}
