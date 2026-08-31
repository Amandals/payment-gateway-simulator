CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT chk_outbox_event_status
        CHECK (status IN ('PENDING','PROCESSED'))
);

CREATE INDEX idx_outbox_event_pending
    ON outbox_event (status, created_at);