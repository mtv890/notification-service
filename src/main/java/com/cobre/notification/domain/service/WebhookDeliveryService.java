package com.cobre.notification.domain.service;

import com.cobre.notification.domain.model.NotificationEvent;
import com.cobre.notification.domain.port.in.DeliverNotificationUseCase;
import com.cobre.notification.domain.port.out.NotificationRepository;
import com.cobre.notification.domain.port.out.WebhookClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WebhookDeliveryService implements DeliverNotificationUseCase {

    private static final Logger logger = LoggerFactory.getLogger(WebhookDeliveryService.class);
    private final WebhookClient webhookClient;
    private final NotificationRepository notificationRepository;

    public WebhookDeliveryService(WebhookClient webhookClient,
                                  NotificationRepository notificationRepository) {
        this.webhookClient = webhookClient;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Async
    @Transactional
    @CircuitBreaker(name = "webhookDelivery", fallbackMethod = "deliveryFallback")
    public void deliver(UUID notificationId) {
        deliverNotification(notificationId);
    }

    public void deliverNotification(UUID notificationId) {
        MDC.put("notificationId", notificationId.toString());

        NotificationEvent event = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        MDC.put("clientId", event.getClientId());
        MDC.put("eventType", event.getEventType());

        logger.info("Attempting delivery (attempt: {})", event.getDeliveryAttempts() + 1);

        try {
            String signature = generateHmacSignature(event.getEventData(), "secret_key_placeholder");

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("X-Webhook-Signature", signature);
            headers.put("X-Event-Type", event.getEventType());
            headers.put("X-Event-Id", event.getId().toString());
            headers.put("X-Idempotency-Key", event.getId().toString());
            headers.put("X-Timestamp", String.valueOf(System.currentTimeMillis()));

            WebhookClient.WebhookResponse response = webhookClient.post(
                    event.getWebhookUrl(),
                    event.getEventData(),
                    headers
            );

            if (response.isSuccess()) {
                event.markAsDelivered(response.getStatusCode());
                logger.info("Successfully delivered notification");
            } else {
                handleDeliveryFailure(event, response.getErrorMessage(), response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Delivery exception", e);
            handleDeliveryFailure(event, e.getMessage(), null);
        }

        notificationRepository.save(event);

        if (event.canRetry()) {
            scheduleRetry(event);
        }

        MDC.clear();
    }

    private void handleDeliveryFailure(NotificationEvent event, String errorMessage, Integer responseCode) {
        event.markAsFailed(errorMessage, responseCode);
        logger.warn("Failed to deliver (attempt: {}, status: {})",
                event.getDeliveryAttempts(), responseCode);
    }

    private void scheduleRetry(NotificationEvent event) {
        long delaySeconds = event.getNextRetryDelaySeconds();
        logger.info("Scheduling retry in {} seconds", delaySeconds);
        // Implementation would send to delayed queue or use scheduling mechanism
    }

    private String generateHmacSignature(String payload, String secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC signature", e);
        }
    }

    private void deliveryFallback(UUID notificationId, Exception e) {
        logger.error("Circuit breaker opened for notification: {}", notificationId, e);
    }
}