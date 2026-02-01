CREATE TABLE outbox_events (
                               id UUID PRIMARY KEY,
                               aggregate_type VARCHAR(255) NOT NULL,
                               aggregate_id VARCHAR(255) NOT NULL,
                               type VARCHAR(255) NOT NULL,
                               payload TEXT NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP WITH TIME ZONE,
                               processed BOOLEAN DEFAULT FALSE,
                               retry_count INT NOT NULL DEFAULT 0,
                               error_message TEXT
);

CREATE INDEX idx_outbox_processed ON outbox_events(processed) WHERE processed = FALSE;
CREATE INDEX idx_outbox_retry ON outbox_events(retry_count) WHERE processed = FALSE;