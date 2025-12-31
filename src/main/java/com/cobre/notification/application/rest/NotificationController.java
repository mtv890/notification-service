package com.cobre.notification.application.rest;

import com.cobre.notification.application.rest.dto.NotificationEventFilter;
import com.cobre.notification.application.rest.dto.NotificationEventResponse;
import com.cobre.notification.domain.port.in.QueryNotificationUseCase;
import com.cobre.notification.domain.port.in.ReplayNotificationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notification_events")
@Tag(name = "Notification Events", description = "Self-service API for event notifications")
@SecurityRequirement(name = "bearer-auth")
@Validated
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final QueryNotificationUseCase queryUseCase;
    private final ReplayNotificationUseCase replayUseCase;

    public NotificationController(QueryNotificationUseCase queryUseCase,
                                  ReplayNotificationUseCase replayUseCase) {
        this.queryUseCase = queryUseCase;
        this.replayUseCase = replayUseCase;
    }

    @GetMapping
    @Operation(summary = "Query event notifications",
            description = "Get all event notifications for authenticated client with optional filters")
    public ResponseEntity<Page<NotificationEventResponse>> getNotificationEvents(
            @RequestParam(required = false) String eventDateFrom,
            @RequestParam(required = false) String eventDateTo,
            @RequestParam(required = false) String deliveryStatus,
            Pageable pageable,
            Authentication authentication) {

        String clientId = extractClientId(authentication);
        logger.info("GET /notification_events for client: {}", clientId);

        NotificationEventFilter filter = NotificationEventFilter.builder()
                .clientId(clientId)
                .eventDateFrom(eventDateFrom)
                .eventDateTo(eventDateTo)
                .deliveryStatus(deliveryStatus)
                .build();

        Page<NotificationEventResponse> events = queryUseCase.queryEvents(filter, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{notification_event_id}")
    @Operation(summary = "Get notification event details",
            description = "Retrieve details of a specific notification event")
    public ResponseEntity<NotificationEventResponse> getNotificationEvent(
            @PathVariable("notification_event_id") UUID notificationEventId,
            Authentication authentication) {

        String clientId = extractClientId(authentication);
        logger.info("GET /notification_events/{} for client: {}", notificationEventId, clientId);

        return queryUseCase.getEventById(notificationEventId, clientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{notification_event_id}/replay")
    @Operation(summary = "Replay failed notification",
            description = "Re-send a notification that has definitely failed")
    public ResponseEntity<NotificationEventResponse> replayNotification(
            @PathVariable("notification_event_id") UUID notificationEventId,
            Authentication authentication) {

        String clientId = extractClientId(authentication);
        logger.info("POST /notification_events/{}/replay for client: {}", notificationEventId, clientId);

        return replayUseCase.replay(notificationEventId, clientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    private String extractClientId(Authentication authentication) {
        return authentication.getName();
    }
}
