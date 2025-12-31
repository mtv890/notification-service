package com.cobre.notification.infrastructure.persistence.jpa;

import com.cobre.notification.domain.model.DeliveryStatus;
import com.cobre.notification.domain.model.NotificationEvent;
import com.cobre.notification.domain.port.out.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaNotificationRepository implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    public JpaNotificationRepository(NotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public NotificationEvent save(NotificationEvent event) {
        NotificationEventEntity entity = toEntity(event);
        NotificationEventEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<NotificationEvent> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<NotificationEvent> findByIdAndClientId(UUID id, String clientId) {
        return jpaRepository.findByIdAndClientId(id, clientId).map(this::toDomain);
    }

    @Override
    public Page<NotificationEvent> findByClientIdWithFilters(String clientId, Instant eventDateFrom,
                                                             Instant eventDateTo, String deliveryStatus,
                                                             Pageable pageable) {
        return jpaRepository.findByClientIdWithFilters(clientId, eventDateFrom, eventDateTo,
                        deliveryStatus, pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<NotificationEvent> findPendingRetries(Instant retryThreshold, Pageable pageable) {
        return jpaRepository.findPendingRetries(retryThreshold, pageable)
                .map(this::toDomain);
    }

    private NotificationEventEntity toEntity(NotificationEvent event) {
        NotificationEventEntity entity = new NotificationEventEntity();
        entity.setId(event.getId());
        entity.setClientId(event.getClientId());
        entity.setEventType(event.getEventType());
        entity.setEventData(event.getEventData());
        entity.setCreatedAt(event.getCreatedAt());
        entity.setWebhookUrl(event.getWebhookUrl());
        entity.setDeliveryStatus(event.getDeliveryStatus().name());
        entity.setDeliveryAttempts(event.getDeliveryAttempts());
        entity.setLastAttemptAt(event.getLastAttemptAt());
        entity.setDeliveredAt(event.getDeliveredAt());
        entity.setErrorMessage(event.getErrorMessage());
        entity.setResponseCode(event.getResponseCode());
        return entity;
    }

    private NotificationEvent toDomain(NotificationEventEntity entity) {
        return new NotificationEvent(
                entity.getId(),
                entity.getClientId(),
                entity.getEventType(),
                entity.getEventData(),
                entity.getCreatedAt(),
                entity.getWebhookUrl(),
                DeliveryStatus.valueOf(entity.getDeliveryStatus()),
                entity.getDeliveryAttempts(),
                entity.getLastAttemptAt(),
                entity.getDeliveredAt(),
                entity.getErrorMessage(),
                entity.getResponseCode()
        );
    }
}
