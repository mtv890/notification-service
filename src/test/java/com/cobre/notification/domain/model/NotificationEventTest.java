package com.cobre.notification.domain.model;


import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificationEventTest {

    @Test
    void shouldCreateNewNotificationEvent() {
        UUID id = UUID.randomUUID();
        NotificationEvent event = new NotificationEvent(
                id, "client-123", "account.balance_updated",
                "{\"balance\": 1000}", "https://webhook.example.com"
        );

        assertEquals(id, event.getId());
        assertEquals("client-123", event.getClientId());
        assertEquals(DeliveryStatus.PENDING, event.getDeliveryStatus());
        assertEquals(0, event.getDeliveryAttempts());
        assertTrue(event.canRetry());
    }

    @Test
    void shouldMarkAsDelivered() {
        NotificationEvent event = createTestEvent();

        event.markAsDelivered(200);

        assertEquals(DeliveryStatus.DELIVERED, event.getDeliveryStatus());
        assertEquals(200, event.getResponseCode());
        assertNotNull(event.getDeliveredAt());
        assertNull(event.getErrorMessage());
    }

    @Test
    void shouldMarkAsFailed() {
        NotificationEvent event = createTestEvent();

        event.markAsFailed("Connection timeout", 0);

        assertEquals(DeliveryStatus.RETRYING, event.getDeliveryStatus());
        assertEquals(1, event.getDeliveryAttempts());
        assertEquals("Connection timeout", event.getErrorMessage());
        assertNotNull(event.getLastAttemptAt());
    }

    @Test
    void shouldTransitionToFailedAfterMaxAttempts() {
        NotificationEvent event = createTestEvent();

        for (int i = 0; i < 7; i++) {
            event.markAsFailed("Error " + i, 500);
        }

        assertEquals(DeliveryStatus.FAILED, event.getDeliveryStatus());
        assertEquals(7, event.getDeliveryAttempts());
        assertFalse(event.canRetry());
    }

    @Test
    void shouldCalculateExponentialBackoff() {
        NotificationEvent event = createTestEvent();

        assertEquals(60, event.getNextRetryDelaySeconds());
        event.markAsFailed("Error", 500);

        assertEquals(300, event.getNextRetryDelaySeconds());
        event.markAsFailed("Error", 500);

        assertEquals(900, event.getNextRetryDelaySeconds());
    }

    @Test
    void shouldResetForReplay() {
        NotificationEvent event = createTestEvent();
        event.markAsFailed("Error", 500);
        event.markAsFailed("Error", 500);

        event.resetForReplay();

        assertEquals(DeliveryStatus.PENDING, event.getDeliveryStatus());
        assertEquals(0, event.getDeliveryAttempts());
        assertNull(event.getLastAttemptAt());
        assertNull(event.getErrorMessage());
    }

    private NotificationEvent createTestEvent() {
        return new NotificationEvent(
                UUID.randomUUID(), "client-123", "test.event",
                "{\"data\": \"test\"}", "https://webhook.test.com"
        );
    }
}