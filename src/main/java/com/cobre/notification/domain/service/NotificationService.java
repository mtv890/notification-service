package com.cobre.notification.domain.service;

import com.cobre.notification.application.rest.dto.NotificationEventFilter;
import com.cobre.notification.application.rest.dto.NotificationEventResponse;
import com.cobre.notification.domain.model.DeliveryStatus;
import com.cobre.notification.domain.model.NotificationEvent;
import com.cobre.notification.domain.port.in.QueryNotificationUseCase;
import com.cobre.notification.domain.port.in.ReplayNotificationUseCase;
import com.cobre.notification.domain.port.out.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService implements QueryNotificationUseCase, ReplayNotificationUseCase {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;
    private final WebhookDeliveryService webhookDeliveryService;

    public NotificationService(NotificationRepository notificationRepository,
                               WebhookDeliveryService webhookDeliveryService) {
        this.notificationRepository = notificationRepository;
        this.webhookDeliveryService = webhookDeliveryService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationEventResponse> queryEvents(NotificationEventFilter filter, Pageable pageable) {
        logger.info("Querying events for client: {}", filter.getClientId());

        Instant dateFrom = filter.getEventDateFrom() != null ?
                Instant.parse(filter.getEventDateFrom()) : null;
        Instant dateTo = filter.getEventDateTo() != null ?
                Instant.parse(filter.getEventDateTo()) : null;

        Page<NotificationEvent> events = notificationRepository.findByClientIdWithFilters(
                filter.getClientId(),
                dateFrom,
                dateTo,
                filter.getDeliveryStatus(),
                pageable
        );

        return events.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationEventResponse> getEventById(UUID eventId, String clientId) {
        logger.info("Getting event {} for client: {}", eventId, clientId);

        return notificationRepository.findByIdAndClientId(eventId, clientId)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public Optional<NotificationEventResponse> replay(UUID eventId, String clientId) {
        logger.info("Replaying event {} for client: {}", eventId, clientId);

        Optional<NotificationEvent> eventOpt = notificationRepository.findByIdAndClientId(eventId, clientId);

        if (eventOpt.isEmpty()) {
            logger.warn("Event {} not found for client {}", eventId, clientId);
            return Optional.empty();
        }

        NotificationEvent event = eventOpt.get();

        if (event.getDeliveryStatus() != DeliveryStatus.FAILED) {
            logger.warn("Cannot replay event {} - status is {}", eventId, event.getDeliveryStatus());
            return Optional.empty();
        }

        event.resetForReplay();
        notificationRepository.save(event);

        // Trigger async delivery
        webhookDeliveryService.deliverNotification(eventId);

        return Optional.of(toResponse(event));
    }

    private NotificationEventResponse toResponse(NotificationEvent event) {
        return new NotificationEventResponse(
                event.getId(),
                event.getClientId(),
                event.getEventType(),
                event.getEventData(),
                event.getCreatedAt(),
                event.getWebhookUrl(),
                event.getDeliveryStatus().name(),
                event.getDeliveryAttempts(),
                event.getLastAttemptAt(),
                event.getDeliveredAt(),
                event.getErrorMessage(),
                event.getResponseCode()
        );
    }
}