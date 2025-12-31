package com.cobre.notification.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class MetricsService {

    private final Counter deliverySuccessCounter;
    private final Counter deliveryFailureCounter;
    private final Timer deliveryTimer;

    public MetricsService(MeterRegistry meterRegistry) {
        this.deliverySuccessCounter = Counter.builder("webhook.delivery.success")
                .description("Number of successful webhook deliveries")
                .tag("type", "webhook")
                .register(meterRegistry);

        this.deliveryFailureCounter = Counter.builder("webhook.delivery.failure")
                .description("Number of failed webhook deliveries")
                .tag("type", "webhook")
                .register(meterRegistry);

        this.deliveryTimer = Timer.builder("webhook.delivery.duration")
                .description("Duration of webhook delivery attempts")
                .register(meterRegistry);
    }

    public void recordSuccess() {
        deliverySuccessCounter.increment();
    }

    public void recordFailure() {
        deliveryFailureCounter.increment();
    }

    public void recordDuration(Duration duration) {
        deliveryTimer.record(duration);
    }
}