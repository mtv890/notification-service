-- File: src/main/resources/db/migration/V1__Initial_Schema.sql

-- Create notification_events table
CREATE TABLE notification_events (
                                     id UUID PRIMARY KEY,
                                     client_id VARCHAR(100) NOT NULL,
                                     event_type VARCHAR(100) NOT NULL,
                                     event_data TEXT NOT NULL,
                                     created_at TIMESTAMP NOT NULL,
                                     webhook_url VARCHAR(500) NOT NULL,
                                     delivery_status VARCHAR(50) NOT NULL,
                                     delivery_attempts INTEGER NOT NULL DEFAULT 0,
                                     last_attempt_at TIMESTAMP,
                                     delivered_at TIMESTAMP,
                                     error_message TEXT,
                                     response_code INTEGER
);

-- Create indexes for efficient querying
CREATE INDEX idx_client_created ON notification_events(client_id, created_at DESC);
CREATE INDEX idx_client_status ON notification_events(client_id, delivery_status);
CREATE INDEX idx_status_attempt ON notification_events(delivery_status, last_attempt_at);

-- Create webhook_subscriptions table
CREATE TABLE webhook_subscriptions (
                                       id UUID PRIMARY KEY,
                                       client_id VARCHAR(100) NOT NULL,
                                       event_type VARCHAR(100) NOT NULL,
                                       webhook_url VARCHAR(500) NOT NULL,
                                       secret_key VARCHAR(255) NOT NULL,
                                       is_active BOOLEAN DEFAULT true,
                                       created_at TIMESTAMP NOT NULL,
                                       UNIQUE(client_id, event_type)
);

CREATE INDEX idx_subscription_client_event ON webhook_subscriptions(client_id, event_type);

-- Add comments for documentation
COMMENT ON TABLE notification_events IS 'Stores all notification events and their delivery status';
COMMENT ON TABLE webhook_subscriptions IS 'Stores client webhook subscriptions';
COMMENT ON COLUMN notification_events.delivery_status IS 'Possible values: PENDING, RETRYING, DELIVERED, FAILED';
COMMENT ON COLUMN notification_events.event_data IS 'JSON payload of the event';