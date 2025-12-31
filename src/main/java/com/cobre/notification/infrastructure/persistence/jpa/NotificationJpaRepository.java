package com.cobre.notification.infrastructure.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationEventEntity, UUID> {

    @Query("SELECT n FROM NotificationEventEntity n WHERE n.clientId = :clientId " +
            "AND (:eventDateFrom IS NULL OR n.createdAt >= :eventDateFrom) " +
            "AND (:eventDateTo IS NULL OR n.createdAt <= :eventDateTo) " +
            "AND (:deliveryStatus IS NULL OR n.deliveryStatus = :deliveryStatus) " +
            "ORDER BY n.createdAt DESC")
    Page<NotificationEventEntity> findByClientIdWithFilters(
            @Param("clientId") String clientId,
            @Param("eventDateFrom") Instant eventDateFrom,
            @Param("eventDateTo") Instant eventDateTo,
            @Param("deliveryStatus") String deliveryStatus,
            Pageable pageable
    );

    Optional<NotificationEventEntity> findByIdAndClientId(UUID id, String clientId);

    @Query("SELECT n FROM NotificationEventEntity n WHERE n.deliveryStatus = 'RETRYING' " +
            "AND n.lastAttemptAt < :retryThreshold ORDER BY n.lastAttemptAt ASC")
    Page<NotificationEventEntity> findPendingRetries(
            @Param("retryThreshold") Instant retryThreshold,
            Pageable pageable
    );
}