package com.cobre.notification.domain.port.in;

import com.cobre.notification.application.rest.dto.NotificationEventResponse;

import java.util.Optional;
import java.util.UUID;

public interface ReplayNotificationUseCase {
    Optional<NotificationEventResponse> replay(UUID eventId, String clientId);
}
