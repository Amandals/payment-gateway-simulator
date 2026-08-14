package com.amanda.paymentgateway.context.impl;

import com.amanda.paymentgateway.entity.Payment;
import com.amanda.paymentgateway.enums.PaymentStatus;
import com.amanda.paymentgateway.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCreationTransactionTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private Clock clock;

    @InjectMocks
    private PaymentCreationTransaction paymentCreationTransaction;

    private static final Instant NOW =
            Instant.parse("2026-08-13T12:00:00Z");

    @Test
    void shouldCreatePaymentAsPending() {
        String idempotencyKey = "idempotency-123";
        Payment payment = new Payment();

        when(clock.instant()).thenReturn(NOW);
        when(paymentService.saveAndFlush(payment)).thenReturn(payment);

        Payment result = paymentCreationTransaction.create(
                idempotencyKey, payment
        );

        assertThat(result).isSameAs(payment);
        assertThat(result.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
        assertThat(result.getUpdatedAt()).isEqualTo(NOW);

        verify(clock).instant();
        verify(paymentService).saveAndFlush(payment);
    }
}