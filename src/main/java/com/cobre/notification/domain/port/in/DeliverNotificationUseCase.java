package com.cobre.notification.domain.port.in;

import java.util.UUID;

public interface DeliverNotificationUseCase {
    void deliver(UUID notificationId);
}