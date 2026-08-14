package com.amanda.paymentgateway.context.impl;

import com.amanda.paymentgateway.entity.Payment;
import com.amanda.paymentgateway.enums.PaymentStatus;
import com.amanda.paymentgateway.exception.BusinessException;
import com.amanda.paymentgateway.service.PaymentService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentContextImplTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private Clock clock;

    @Mock
    private PaymentCreationTransaction paymentCreationTransaction;

    @InjectMocks
    private PaymentContextImpl paymentContext;

    private static final Instant NOW = Instant.parse("2026-08-13T12:00:00Z");

    @Nested
    class Create {

        @Test
        void shouldReturnExistingPaymentWhenIdempotencyKeyAlreadyExists() {
            String idempotencyKey = "idempotency-123";

            Payment existingPayment = new Payment();
            existingPayment.setId(UUID.randomUUID());
            existingPayment.setIdempotencyKey(idempotencyKey);
            existingPayment.setStatus(PaymentStatus.PENDING);

            Payment newPayment = new Payment();

            when(paymentService.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingPayment));

            Payment result = paymentContext.create(idempotencyKey, newPayment);

            assertThat(result).isSameAs(existingPayment);

            verify(paymentService).findByIdempotencyKey(idempotencyKey);
            verifyNoInteractions(paymentCreationTransaction);
        }

        @Test
        void shouldCreatePaymentWhenIdempotencyKeyDoesNotExist() {
            String idempotencyKey = "idempotency-123";

            Payment payment = new Payment();
            Payment createdPayment = new Payment();

            when(paymentService.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
            when(paymentCreationTransaction.create(idempotencyKey, payment)).thenReturn(createdPayment);

            Payment result = paymentContext.create(
                    idempotencyKey,
                    payment
            );

            assertThat(result).isSameAs(createdPayment);

            verify(paymentService).findByIdempotencyKey(idempotencyKey);
            verify(paymentCreationTransaction).create(idempotencyKey, payment);
        }

        @Test
        void shouldReturnExistingPaymentWhenCreationFailsDueToDuplicateIdempotencyKey() {
            String idempotencyKey = "idempotency-123";

            Payment payment = new Payment();

            Payment existingPayment = new Payment();
            existingPayment.setId(UUID.randomUUID());
            existingPayment.setIdempotencyKey(idempotencyKey);
            existingPayment.setStatus(PaymentStatus.PENDING);

            boolean[] firstCall = {true};

            when(paymentService.findByIdempotencyKey(idempotencyKey))
                    .thenAnswer(invocation -> {
                        if (firstCall[0]) {
                            firstCall[0] = false;
                            return Optional.empty();
                        }

                        return Optional.of(existingPayment);
                    });

            when(paymentCreationTransaction.create(idempotencyKey, payment))
                    .thenThrow(new DataIntegrityViolationException(
                            "Duplicate idempotency key"
                    ));

            Payment result = paymentContext.create(idempotencyKey, payment);

            assertThat(result).isSameAs(existingPayment);

            verify(paymentService, times(2))
                    .findByIdempotencyKey(idempotencyKey);

            verify(paymentCreationTransaction)
                    .create(idempotencyKey, payment);
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldFindPaymentById() {
            UUID id = UUID.randomUUID();

            Payment payment = new Payment();
            payment.setId(id);

            when(paymentService.findById(id)).thenReturn(payment);

            Payment result = paymentContext.findById(id);

            assertThat(result).isSameAs(payment);

            verify(paymentService).findById(id);
        }
    }

    @Nested
    class Authorize {

        @Test
        void shouldAuthorizePendingPayment() {
            UUID id = UUID.randomUUID();

            Payment payment = new Payment();
            payment.setId(id);
            payment.setStatus(PaymentStatus.PENDING);

            when(paymentService.findById(id)).thenReturn(payment);
            when(clock.instant()).thenReturn(NOW);
            when(paymentService.save(payment)).thenReturn(payment);

            Payment result = paymentContext.authorize(id);

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
            assertThat(result.getUpdatedAt()).isEqualTo(NOW);

            verify(paymentService).findById(id);
            verify(clock).instant();
            verify(paymentService).save(payment);
        }

        @ParameterizedTest
        @EnumSource(
                value = PaymentStatus.class,
                names = "PENDING",
                mode = EnumSource.Mode.EXCLUDE
        )
        void shouldNotAuthorizeNonPendingPayment(PaymentStatus status) {
            UUID id = UUID.randomUUID();

            Payment payment = new Payment();
            payment.setId(id);
            payment.setStatus(status);

            when(paymentService.findById(id)).thenReturn(payment);

            assertThatThrownBy(() -> paymentContext.authorize(id))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessException.PAYMENT_NOT_AUTHORIZED);

            verify(paymentService).findById(id);
            verifyNoInteractions(clock);
            verify(paymentService, never()).save(any());
        }
    }

    @Nested
    class Cancel {

        @ParameterizedTest
        @EnumSource(
                value = PaymentStatus.class,
                names = {"PENDING", "AUTHORIZED"},
                mode = EnumSource.Mode.EXCLUDE
        )
        void shouldNotCancelInvalidPayment(PaymentStatus status) {
            UUID id = UUID.randomUUID();

            Payment payment = new Payment();
            payment.setId(id);
            payment.setStatus(status);

            when(paymentService.findById(id)).thenReturn(payment);

            assertThatThrownBy(() -> paymentContext.cancel(id))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessException.PAYMENT_NOT_CANCELLABLE);

            verify(paymentService).findById(id);
            verifyNoInteractions(clock);
            verify(paymentService, never()).save(any());
        }

        @ParameterizedTest
        @EnumSource(
                value = PaymentStatus.class,
                names = {"PENDING", "AUTHORIZED"}
        )
        void shouldCancelPendingOrAuthorizedPayment(PaymentStatus status) {
            UUID id = UUID.randomUUID();

            Payment payment = new Payment();
            payment.setId(id);
            payment.setStatus(status);

            when(paymentService.findById(id)).thenReturn(payment);
            when(clock.instant()).thenReturn(NOW);
            when(paymentService.save(payment)).thenReturn(payment);

            Payment result = paymentContext.cancel(id);

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(result.getUpdatedAt()).isEqualTo(NOW);

            verify(paymentService).findById(id);
            verify(clock).instant();
            verify(paymentService).save(payment);
        }
    }

    @Nested
    class Decline {

        @Test
        void shouldDeclinePendingPayment() {
            UUID id = UUID.randomUUID();

            Payment payment = new Payment();
            payment.setId(id);
            payment.setStatus(PaymentStatus.PENDING);

            when(paymentService.findById(id)).thenReturn(payment);
            when(clock.instant()).thenReturn(NOW);
            when(paymentService.save(payment)).thenReturn(payment);

            Payment result = paymentContext.decline(id);

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.DECLINED);
            assertThat(result.getUpdatedAt()).isEqualTo(NOW);

            verify(paymentService).findById(id);
            verify(clock).instant();
            verify(paymentService).save(payment);
        }

        @ParameterizedTest
        @EnumSource(
                value = PaymentStatus.class,
                names = "PENDING",
                mode = EnumSource.Mode.EXCLUDE
        )
        void shouldNotDeclineNonPendingPayment(PaymentStatus status) {
            UUID id = UUID.randomUUID();

            Payment payment = new Payment();
            payment.setId(id);
            payment.setStatus(status);

            when(paymentService.findById(id)).thenReturn(payment);

            assertThatThrownBy(() -> paymentContext.decline(id))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessException.PAYMENT_NOT_DECLINABLE);

            verify(paymentService).findById(id);
            verifyNoInteractions(clock);
            verify(paymentService, never()).save(any());
        }
    }

    @Nested
    class Refund {

        @Test
        void shouldRefundAuthorizedPayment() {
            UUID id = UUID.randomUUID();

            Payment payment = new Payment();
            payment.setId(id);
            payment.setStatus(PaymentStatus.AUTHORIZED);

            when(paymentService.findById(id)).thenReturn(payment);
            when(clock.instant()).thenReturn(NOW);
            when(paymentService.save(payment)).thenReturn(payment);

            Payment result = paymentContext.refund(id);

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(result.getUpdatedAt()).isEqualTo(NOW);

            verify(paymentService).findById(id);
            verify(clock).instant();
            verify(paymentService).save(payment);
        }

        @ParameterizedTest
        @EnumSource(
                value = PaymentStatus.class,
                names = "AUTHORIZED",
                mode = EnumSource.Mode.EXCLUDE
        )
        void shouldNotRefundNonAuthorizedPayment(PaymentStatus status) {
            UUID id = UUID.randomUUID();

            Payment payment = new Payment();
            payment.setId(id);
            payment.setStatus(status);

            when(paymentService.findById(id)).thenReturn(payment);

            assertThatThrownBy(() -> paymentContext.refund(id))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessException.PAYMENT_NOT_REFUNDABLE);

            verify(paymentService).findById(id);
            verifyNoInteractions(clock);
            verify(paymentService, never()).save(any());
        }
    }
}