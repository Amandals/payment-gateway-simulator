package com.amanda.paymentgateway.context.impl;

import com.amanda.paymentgateway.entity.OutboxEvent;
import com.amanda.paymentgateway.entity.Payment;
import com.amanda.paymentgateway.enums.OutboxEventStatus;
import com.amanda.paymentgateway.enums.OutboxEventType;
import com.amanda.paymentgateway.enums.PaymentStatus;
import com.amanda.paymentgateway.outbox.OutboxEventFactory;
import com.amanda.paymentgateway.outbox.OutboxPayloadSerializer;
import com.amanda.paymentgateway.outbox.PaymentCreatedEvent;
import com.amanda.paymentgateway.repository.OutboxEventRepository;
import com.amanda.paymentgateway.service.OutboxEventService;
import com.amanda.paymentgateway.service.PaymentService;
import com.amanda.paymentgateway.service.impl.OutboxEventServiceImpl;
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
    private final OutboxEventService outboxEventService;
    private final OutboxEventFactory outboxEventFactory;
    private final OutboxPayloadSerializer outboxPayloadSerializer;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment create(String idempotencyKey, Payment payment) {
        Instant now = clock.instant();

        payment.setIdempotencyKey(idempotencyKey);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);

        Payment savedPayment = paymentService.saveAndFlush(payment);
        PaymentCreatedEvent event = new PaymentCreatedEvent(
                savedPayment.getId(),
                savedPayment.getAmount(),
                savedPayment.getCurrency(),
                savedPayment.getStatus()
        );

        String payload = outboxPayloadSerializer.serialize(event);

        OutboxEvent outboxEvent = outboxEventFactory.create(
                savedPayment.getId(),
                OutboxEventType.PAYMENT_CREATED,
                payload
        );

        outboxEventService.save(outboxEvent);

        return savedPayment;
    }
}