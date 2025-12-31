package com.cobre.notification.domain.port.out;

import com.cobre.notification.domain.model.NotificationEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {
    NotificationEvent save(NotificationEvent event);
    Optional<NotificationEvent> findById(UUID id);
    Optional<NotificationEvent> findByIdAndClientId(UUID id, String clientId);
    Page<NotificationEvent> findByClientIdWithFilters(
            String clientId,
            Instant eventDateFrom,
            Instant eventDateTo,
            String deliveryStatus,
            Pageable pageable
    );
    Page<NotificationEvent> findPendingRetries(Instant retryThreshold, Pageable pageable);
}
