package com.cobre.notification.application.rest.dto;

import lombok.Getter;

@Getter
public class NotificationEventFilter {
    private String clientId;
    private String eventDateFrom;
    private String eventDateTo;
    private String deliveryStatus;

    private NotificationEventFilter(Builder builder) {
        this.clientId = builder.clientId;
        this.eventDateFrom = builder.eventDateFrom;
        this.eventDateTo = builder.eventDateTo;
        this.deliveryStatus = builder.deliveryStatus;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String clientId;
        private String eventDateFrom;
        private String eventDateTo;
        private String deliveryStatus;

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder eventDateFrom(String eventDateFrom) {
            this.eventDateFrom = eventDateFrom;
            return this;
        }

        public Builder eventDateTo(String eventDateTo) {
            this.eventDateTo = eventDateTo;
            return this;
        }

        public Builder deliveryStatus(String deliveryStatus) {
            this.deliveryStatus = deliveryStatus;
            return this;
        }

        public NotificationEventFilter build() {
            return new NotificationEventFilter(this);
        }
    }
}
