package com.cobre.notification.domain.port.out;

import com.cobre.notification.domain.model.WebhookSubscription;

import java.util.Optional;

public interface SubscriptionRepository {
    Optional<WebhookSubscription> findByClientIdAndEventType(String clientId, String eventType);
}