package com.cobre.notification.domain.service;

import com.cobre.notification.domain.model.DeliveryStatus;
import com.cobre.notification.domain.model.NotificationEvent;
import com.cobre.notification.domain.port.out.NotificationRepository;
import com.cobre.notification.domain.port.out.WebhookClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookDeliveryServiceTest {

    @Mock
    private WebhookClient webhookClient;

    @Mock
    private NotificationRepository notificationRepository;

    private WebhookDeliveryService service;

    @BeforeEach
    void setUp() {
        service = new WebhookDeliveryService(webhookClient, notificationRepository);
    }

    @Test
    void shouldDeliverNotificationSuccessfully() {
        UUID id = UUID.randomUUID();
        NotificationEvent event = new NotificationEvent(
                id, "client-123", "test.event",
                "{\"data\": \"test\"}", "https://webhook.test.com"
        );

        when(notificationRepository.findById(id)).thenReturn(Optional.of(event));
        when(webhookClient.post(anyString(), anyString(), anyMap()))
                .thenReturn(new WebhookClient.WebhookResponse(200, "OK", null, true));

        service.deliverNotification(id);

        verify(webhookClient).post(eq("https://webhook.test.com"), anyString(), anyMap());
        verify(notificationRepository).save(argThat(e ->
                e.getDeliveryStatus() == DeliveryStatus.DELIVERED));
    }

    @Test
    void shouldHandleDeliveryFailure() {
        UUID id = UUID.randomUUID();
        NotificationEvent event = new NotificationEvent(
                id, "client-123", "test.event",
                "{\"data\": \"test\"}", "https://webhook.test.com"
        );

        when(notificationRepository.findById(id)).thenReturn(Optional.of(event));
        when(webhookClient.post(anyString(), anyString(), anyMap()))
                .thenReturn(new WebhookClient.WebhookResponse(500, null, "Server Error", false));

        service.deliverNotification(id);

        verify(notificationRepository).save(argThat(e ->
                e.getDeliveryStatus() == DeliveryStatus.RETRYING &&
                        e.getDeliveryAttempts() == 1));
    }

    @Test
    void shouldHandleNetworkException() {
        UUID id = UUID.randomUUID();
        NotificationEvent event = new NotificationEvent(
                id, "client-123", "test.event",
                "{\"data\": \"test\"}", "https://webhook.test.com"
        );

        when(notificationRepository.findById(id)).thenReturn(Optional.of(event));
        when(webhookClient.post(anyString(), anyString(), anyMap()))
                .thenThrow(new RuntimeException("Connection timeout"));

        service.deliverNotification(id);

        verify(notificationRepository).save(argThat(e ->
                e.getDeliveryStatus() == DeliveryStatus.RETRYING));
    }
}