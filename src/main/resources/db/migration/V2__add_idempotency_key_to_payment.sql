ALTER TABLE payment
    ADD COLUMN idempotency_key VARCHAR(255) NOT NULL;

ALTER TABLE payment
    ADD CONSTRAINT uk_payments_idempotency_key
        UNIQUE (idempotency_key);