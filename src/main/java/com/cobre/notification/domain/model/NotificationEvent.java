package com.cobre.notification.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;


@Getter
public class NotificationEvent {
    // Getters and Setters
    private UUID id;
    private String clientId;
    private String eventType;
    private String eventData; // JSON string
    private Instant createdAt;
    private String webhookUrl;
    private DeliveryStatus deliveryStatus;
    private int deliveryAttempts;
    private Instant lastAttemptAt;
    private Instant deliveredAt;
    private String errorMessage;
    private Integer responseCode;

    // Constructor
    public NotificationEvent(UUID id, String clientId, String eventType,
                             String eventData, String webhookUrl) {
        this.id = id;
        this.clientId = clientId;
        this.eventType = eventType;
        this.eventData = eventData;
        this.webhookUrl = webhookUrl;
        this.createdAt = Instant.now();
        this.deliveryStatus = DeliveryStatus.PENDING;
        this.deliveryAttempts = 0;
    }

    // For reconstruction from persistence
    public NotificationEvent(UUID id, String clientId, String eventType, String eventData,
                             Instant createdAt, String webhookUrl, DeliveryStatus deliveryStatus,
                             int deliveryAttempts, Instant lastAttemptAt, Instant deliveredAt,
                             String errorMessage, Integer responseCode) {
        this.id = id;
        this.clientId = clientId;
        this.eventType = eventType;
        this.eventData = eventData;
        this.createdAt = createdAt;
        this.webhookUrl = webhookUrl;
        this.deliveryStatus = deliveryStatus;
        this.deliveryAttempts = deliveryAttempts;
        this.lastAttemptAt = lastAttemptAt;
        this.deliveredAt = deliveredAt;
        this.errorMessage = errorMessage;
        this.responseCode = responseCode;
    }

    // Business logic methods
    public void markAsDelivered(int responseCode) {
        this.deliveryStatus = DeliveryStatus.DELIVERED;
        this.deliveredAt = Instant.now();
        this.responseCode = responseCode;
        this.errorMessage = null;
    }

    public void markAsFailed(String errorMessage, Integer responseCode) {
        this.deliveryAttempts++;
        this.lastAttemptAt = Instant.now();
        this.errorMessage = errorMessage;
        this.responseCode = responseCode;

        if (this.deliveryAttempts >= 7) {
            this.deliveryStatus = DeliveryStatus.FAILED;
        } else {
            this.deliveryStatus = DeliveryStatus.RETRYING;
        }
    }

    public boolean canRetry() {
        return deliveryAttempts < 7 &&
                deliveryStatus != DeliveryStatus.DELIVERED;
    }

    public long getNextRetryDelaySeconds() {
        // Exponential backoff: 60s, 300s, 900s, 3600s, 14400s, 43200s
        int[] delays = {60, 300, 900, 3600, 14400, 43200};
        int index = Math.min(deliveryAttempts, delays.length - 1);
        return delays[index];
    }

    public void resetForReplay() {
        this.deliveryStatus = DeliveryStatus.PENDING;
        this.deliveryAttempts = 0;
        this.lastAttemptAt = null;
        this.deliveredAt = null;
        this.errorMessage = null;
        this.responseCode = null;
    }

    public void setDeliveryStatus(DeliveryStatus status) {
        this.deliveryStatus = status;
    }
}