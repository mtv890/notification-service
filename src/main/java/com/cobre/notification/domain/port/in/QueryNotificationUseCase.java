package com.cobre.notification.domain.port.in;

import com.cobre.notification.application.rest.dto.NotificationEventFilter;
import com.cobre.notification.application.rest.dto.NotificationEventResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface QueryNotificationUseCase {
    Page<NotificationEventResponse> queryEvents(NotificationEventFilter filter, Pageable pageable);
    Optional<NotificationEventResponse> getEventById(UUID eventId, String clientId);
}
