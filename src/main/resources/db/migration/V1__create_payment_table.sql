CREATE TABLE payment (
    id UUID PRIMARY KEY,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_payment_status
        CHECK (status IN (
          'PENDING',
          'AUTHORIZED',
          'DECLINED',
          'CANCELLED',
          'REFUNDED'
        ))
);