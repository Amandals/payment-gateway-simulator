package com.amanda.paymentgateway.outbox;

import com.amanda.paymentgateway.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCreatedEvent(
        UUID paymentId,
        BigDecimal amount,
        String currency,
        PaymentStatus status
) {
}