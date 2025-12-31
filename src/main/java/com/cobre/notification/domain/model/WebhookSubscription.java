package com.cobre.notification.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class WebhookSubscription {
    private UUID id;
    private String clientId;
    private String eventType;
    private String webhookUrl;
    private String secretKey;
    private boolean active;
    private Instant createdAt;

    public WebhookSubscription(UUID id, String clientId, String eventType,
                               String webhookUrl, String secretKey) {
        this.id = id;
        this.clientId = clientId;
        this.eventType = eventType;
        this.webhookUrl = webhookUrl;
        this.secretKey = secretKey;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public boolean isActiveFor(String clientId, String eventType) {
        return this.active &&
                this.clientId.equals(clientId) &&
                this.eventType.equals(eventType);
    }

    public void setActive(boolean active) { this.active = active; }
}
