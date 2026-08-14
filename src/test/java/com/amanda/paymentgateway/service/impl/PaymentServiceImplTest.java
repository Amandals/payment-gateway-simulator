package com.amanda.paymentgateway.service.impl;

import com.amanda.paymentgateway.context.impl.PaymentCreationTransaction;
import com.amanda.paymentgateway.entity.Payment;
import com.amanda.paymentgateway.exception.BusinessException;
import com.amanda.paymentgateway.repository.PaymentRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Nested
    class Save {

        @Test
        void shouldSavePayment() {
            Payment payment = new Payment();

            when(paymentRepository.save(payment)).thenReturn(payment);

            Payment result = paymentService.save(payment);

            assertThat(result).isEqualTo(payment);

            verify(paymentRepository).save(payment);
        }
    }

    @Nested
    class SaveAndFlush {

        @Test
        void shouldSaveAndFlushPayment() {
            Payment payment = new Payment();

            when(paymentRepository.saveAndFlush(payment))
                    .thenReturn(payment);

            Payment result = paymentService.saveAndFlush(payment);

            assertThat(result).isSameAs(payment);

            verify(paymentRepository).saveAndFlush(payment);
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldFindPaymentById() {
            UUID id = UUID.randomUUID();

            Payment payment = new Payment();
            payment.setId(id);

            when(paymentRepository.findById(id)).thenReturn(Optional.of(payment));

            Payment result = paymentService.findById(id);

            assertThat(result).isSameAs(payment);
            assertThat(result.getId()).isEqualTo(id);

            verify(paymentRepository).findById(id);
        }

        @Test
        void shouldThrowExceptionWhenPaymentDoesNotExist() {
            UUID id = UUID.randomUUID();

            when(paymentRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.findById(id))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessException.PAYMENT_NOT_FOUND);

            verify(paymentRepository).findById(id);
        }
    }
    @Nested
    class FindByIdempotencyKey {

        @Test
        void shouldFindPaymentByIdempotencyKey() {
            String idempotencyKey = UUID.randomUUID().toString();

            Payment payment = new Payment();
            payment.setId(UUID.randomUUID());

            when(paymentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(payment));

            Optional<Payment> result = paymentService.findByIdempotencyKey(idempotencyKey);

            assertThat(result)
                    .isPresent()
                    .containsSame(payment);

            verify(paymentRepository).findByIdempotencyKey(idempotencyKey);
        }

        @Test
        void shouldReturnEmptyWhenIdempotencyKeyDoesNotExist() {
            String idempotencyKey = UUID.randomUUID().toString();

            when(paymentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());

            Optional<Payment> result = paymentService.findByIdempotencyKey(idempotencyKey);

            assertThat(result).isEmpty();

            verify(paymentRepository).findByIdempotencyKey(idempotencyKey);
        }
    }

}