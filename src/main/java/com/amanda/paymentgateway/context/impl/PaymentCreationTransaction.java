package com.amanda.paymentgateway.context.impl;

import com.amanda.paymentgateway.entity.Payment;
import com.amanda.paymentgateway.enums.PaymentStatus;
import com.amanda.paymentgateway.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class PaymentCreationTransaction {

    private final PaymentService paymentService;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment create(String idempotencyKey, Payment payment) {
        Instant now = clock.instant();

        payment.setIdempotencyKey(idempotencyKey);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);

        return paymentService.saveAndFlush(payment);
    }
}