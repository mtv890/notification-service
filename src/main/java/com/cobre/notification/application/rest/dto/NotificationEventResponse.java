package com.cobre.notification.application.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationEventResponse {
    private UUID id;
    private String clientId;
    private String eventType;
    private String eventData;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    private String webhookUrl;
    private String deliveryStatus;
    private int deliveryAttempts;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant lastAttemptAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant deliveredAt;

    private String errorMessage;
    private Integer responseCode;

    public NotificationEventResponse(UUID id, String clientId, String eventType, String eventData,
                                     Instant createdAt, String webhookUrl, String deliveryStatus,
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

    // Getters
    public UUID getId() { return id; }
    public String getClientId() { return clientId; }
    public String getEventType() { return eventType; }
    public String getEventData() { return eventData; }
    public Instant getCreatedAt() { return createdAt; }
    public String getWebhookUrl() { return webhookUrl; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public int getDeliveryAttempts() { return deliveryAttempts; }
    public Instant getLastAttemptAt() { return lastAttemptAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public String getErrorMessage() { return errorMessage; }
    public Integer getResponseCode() { return responseCode; }
}