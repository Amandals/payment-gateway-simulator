package com.amanda.paymentgateway.context.impl;

import com.amanda.paymentgateway.entity.OutboxEvent;
import com.amanda.paymentgateway.entity.Payment;
import com.amanda.paymentgateway.enums.OutboxEventType;
import com.amanda.paymentgateway.enums.PaymentStatus;
import com.amanda.paymentgateway.outbox.OutboxEventFactory;
import com.amanda.paymentgateway.outbox.OutboxPayloadSerializer;
import com.amanda.paymentgateway.outbox.PaymentCreatedEvent;
import com.amanda.paymentgateway.service.OutboxEventService;
import com.amanda.paymentgateway.service.PaymentService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCreationTransactionTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private OutboxEventFactory outboxEventFactory;

    @Mock
    private OutboxPayloadSerializer outboxPayloadSerializer;

    private Clock clock;

    private PaymentCreationTransaction paymentCreationTransaction;

    private final Instant fixedTime =
            Instant.parse("2026-08-14T15:00:00Z");

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(fixedTime, ZoneOffset.UTC);

        paymentCreationTransaction =
                new PaymentCreationTransaction(
                        paymentService,
                        clock,
                        outboxEventService,
                        outboxEventFactory,
                        outboxPayloadSerializer
                );
    }

    @Nested
    class CreatePayment {

        @Test
        void shouldCreatePaymentAndOutboxEvent() {
            String idempotencyKey = "idempotency-123";
            UUID paymentId = UUID.randomUUID();

            Payment payment = new Payment();
            payment.setAmount(new BigDecimal("100.00"));
            payment.setCurrency("BRL");
            payment.setDescription("Test payment");

            payment.setId(paymentId);

            OutboxEvent outboxEvent = new OutboxEvent();

            when(paymentService.saveAndFlush(payment)).thenReturn(payment);
            when(outboxPayloadSerializer.serialize(any(PaymentCreatedEvent.class)))
                    .thenReturn("payment-payload");
            when(outboxEventFactory.create(
                    eq(paymentId), eq(OutboxEventType.PAYMENT_CREATED), eq("payment-payload")))
                    .thenReturn(outboxEvent);
            when(outboxEventService.save(outboxEvent)).thenReturn(outboxEvent);

            Payment result = paymentCreationTransaction.create(idempotencyKey, payment);

            assertThat(result).isSameAs(payment);
            assertThat(payment.getIdempotencyKey()).isEqualTo(idempotencyKey);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getCreatedAt()).isEqualTo(fixedTime);
            assertThat(payment.getUpdatedAt()).isEqualTo(fixedTime);

            verify(paymentService).saveAndFlush(payment);
            verify(outboxPayloadSerializer).serialize(any(PaymentCreatedEvent.class));
            verify(outboxEventFactory)
                    .create(
                            eq(paymentId),
                            eq(OutboxEventType.PAYMENT_CREATED),
                            eq("payment-payload")
                    );
            verify(outboxEventService).save(outboxEvent);
        }

        @Test
        void shouldCreatePaymentCreatedEventWithPaymentData() {
            String idempotencyKey = "idempotency-123";
            UUID paymentId = UUID.randomUUID();

            Payment payment = new Payment();
            payment.setAmount(new BigDecimal("250.50"));
            payment.setCurrency("BRL");

            OutboxEvent outboxEvent = new OutboxEvent();

            when(paymentService.saveAndFlush(payment))
                    .thenAnswer(invocation -> {
                        Payment saved = invocation.getArgument(0);
                        saved.setId(paymentId);
                        return saved;
                    });
            when(outboxPayloadSerializer.serialize(
                    any(PaymentCreatedEvent.class)))
                    .thenReturn("payload");
            when(outboxEventFactory.create(
                    eq(paymentId),
                    eq(OutboxEventType.PAYMENT_CREATED),
                    eq("payload")
            )).thenReturn(outboxEvent);

            ArgumentCaptor<PaymentCreatedEvent> captor = ArgumentCaptor.forClass(PaymentCreatedEvent.class);

            paymentCreationTransaction.create(idempotencyKey, payment);

            verify(outboxPayloadSerializer).serialize(captor.capture());

            PaymentCreatedEvent event = captor.getValue();

            assertThat(event.paymentId()).isEqualTo(paymentId);
            assertThat(event.amount()).isEqualByComparingTo("250.50");
            assertThat(event.currency()).isEqualTo("BRL");
            assertThat(event.status()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        void shouldSaveCreatedOutboxEvent() {
            String idempotencyKey = "idempotency-123";
            UUID paymentId = UUID.randomUUID();
            Payment payment = new Payment();

            when(paymentService.saveAndFlush(payment))
                    .thenAnswer(invocation -> {
                        Payment saved = invocation.getArgument(0);
                        saved.setId(paymentId);
                        return saved;
                    });

            when(outboxPayloadSerializer.serialize(any(PaymentCreatedEvent.class))).thenReturn("payload");

            OutboxEvent outboxEvent = new OutboxEvent();

            when(outboxEventFactory.create(
                    paymentId, OutboxEventType.PAYMENT_CREATED, "payload"
            )).thenReturn(outboxEvent);

            paymentCreationTransaction.create(idempotencyKey, payment);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);

            verify(outboxEventService).save(captor.capture());

            assertThat(captor.getValue()).isSameAs(outboxEvent);
        }
    }

    @Nested
    class PaymentFailure {

        @Test
        void shouldNotCreateOutboxEventWhenPaymentSaveFails() {
            String idempotencyKey = "idempotency-123";
            Payment payment = new Payment();
            RuntimeException exception = new RuntimeException("Database error");

            when(paymentService.saveAndFlush(payment)).thenThrow(exception);

            Throwable thrown =
                    Assertions.catchThrowable(() ->
                            paymentCreationTransaction.create(
                                    idempotencyKey,
                                    payment
                            )
                    );

            assertThat(thrown).isSameAs(exception);

            verify(paymentService).saveAndFlush(payment);
            verifyNoInteractions(outboxPayloadSerializer, outboxEventFactory, outboxEventService);
        }
    }

    @Nested
    class OutboxFailure {

        @Test
        void shouldPropagateSerializationException() {
            String idempotencyKey = "idempotency-123";
            UUID paymentId = UUID.randomUUID();
            Payment payment = new Payment();
            RuntimeException exception = new RuntimeException("Serialization error");

            when(paymentService.saveAndFlush(payment))
                    .thenAnswer(invocation -> {
                        Payment saved = invocation.getArgument(0);
                        saved.setId(paymentId);
                        return saved;
                    });
            when(outboxPayloadSerializer.serialize(any(PaymentCreatedEvent.class))).thenThrow(exception);

            Throwable thrown = Assertions.catchThrowable(() -> paymentCreationTransaction.create(idempotencyKey, payment));

            assertThat(thrown).isSameAs(exception);

            verify(paymentService).saveAndFlush(payment);
            verify(outboxPayloadSerializer).serialize(any(PaymentCreatedEvent.class));
            verifyNoInteractions(outboxEventFactory, outboxEventService);
        }

        @Test
        void shouldPropagateOutboxSaveException() {
            String idempotencyKey = "idempotency-123";
            UUID paymentId = UUID.randomUUID();
            Payment payment = new Payment();

            OutboxEvent outboxEvent = new OutboxEvent();
            RuntimeException exception = new RuntimeException("Outbox database error");

            when(paymentService.saveAndFlush(payment))
                    .thenAnswer(invocation -> {
                        Payment saved = invocation.getArgument(0);
                        saved.setId(paymentId);
                        return saved;
                    });

            when(outboxPayloadSerializer.serialize(any(PaymentCreatedEvent.class))).thenReturn("payload");
            when(outboxEventFactory.create(paymentId, OutboxEventType.PAYMENT_CREATED, "payload"))
                    .thenReturn(outboxEvent);
            when(outboxEventService.save(outboxEvent)).thenThrow(exception);

            Throwable thrown =
                    Assertions.catchThrowable(() ->
                            paymentCreationTransaction.create(idempotencyKey, payment)
                    );

            assertThat(thrown).isSameAs(exception);

            verify(paymentService).saveAndFlush(payment);
            verify(outboxEventService).save(outboxEvent);
        }
    }
}