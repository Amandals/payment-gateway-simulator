package com.amanda.paymentgateway.facade.impl;

import com.amanda.paymentgateway.context.PaymentContext;
import com.amanda.paymentgateway.dto.request.PaymentRequest;
import com.amanda.paymentgateway.dto.response.PaymentResponse;
import com.amanda.paymentgateway.entity.Payment;
import com.amanda.paymentgateway.facade.mapper.PaymentMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentFacadeImplTest {

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentContext paymentContext;

    @InjectMocks
    private PaymentFacadeImpl paymentFacade;

    @Nested
    class Create {

        @Test
        void shouldCreatePayment() {
            PaymentRequest request = new PaymentRequest();
            Payment payment = new Payment();
            Payment createdPayment = new Payment();
            PaymentResponse response = new PaymentResponse();
            String idempotencyKey = "idem-123";

            when(paymentMapper.toEntity(request)).thenReturn(payment);
            when(paymentContext.create(idempotencyKey, payment)).thenReturn(createdPayment);
            when(paymentMapper.toResponse(createdPayment)).thenReturn(response);

            PaymentResponse result = paymentFacade.create(idempotencyKey, request);

            assertThat(result).isSameAs(response);

            verify(paymentMapper).toEntity(request);
            verify(paymentContext).create(idempotencyKey, payment);
            verify(paymentMapper).toResponse(createdPayment);
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldFindPaymentById() {
            UUID id = UUID.randomUUID();

            Payment payment = new Payment();
            PaymentResponse response = new PaymentResponse();

            when(paymentContext.findById(id)).thenReturn(payment);
            when(paymentMapper.toResponse(payment)).thenReturn(response);

            PaymentResponse result = paymentFacade.findById(id);

            assertThat(result).isSameAs(response);

            verify(paymentContext).findById(id);
            verify(paymentMapper).toResponse(payment);
        }
    }

    @Nested
    class Authorize {

        @Test
        void shouldAuthorizePayment() {
            UUID id = UUID.randomUUID();

            Payment payment = new Payment();
            PaymentResponse response = new PaymentResponse();

            when(paymentContext.authorize(id)).thenReturn(payment);
            when(paymentMapper.toResponse(payment)).thenReturn(response);

            PaymentResponse result = paymentFacade.authorize(id);

            assertThat(result).isSameAs(response);

            verify(paymentContext).authorize(id);
            verify(paymentMapper).toResponse(payment);
        }
    }

    @Nested
    class Cancel {

        @Test
        void shouldCancelPayment() {
            UUID id = UUID.randomUUID();

            Payment payment = new Payment();
            PaymentResponse response = new PaymentResponse();

            when(paymentContext.cancel(id)).thenReturn(payment);
            when(paymentMapper.toResponse(payment)).thenReturn(response);

            PaymentResponse result = paymentFacade.cancel(id);

            assertThat(result).isSameAs(response);

            verify(paymentContext).cancel(id);
            verify(paymentMapper).toResponse(payment);
        }
    }

    @Nested
    class Decline {

        @Test
        void shouldDeclinePayment() {
            UUID id = UUID.randomUUID();

            Payment payment = new Payment();
            PaymentResponse response = new PaymentResponse();

            when(paymentContext.decline(id)).thenReturn(payment);
            when(paymentMapper.toResponse(payment)).thenReturn(response);

            PaymentResponse result = paymentFacade.decline(id);

            assertThat(result).isSameAs(response);

            verify(paymentContext).decline(id);
            verify(paymentMapper).toResponse(payment);
        }
    }

    @Nested
    class Refund {

        @Test
        void shouldRefundPayment() {
            UUID id = UUID.randomUUID();

            Payment payment = new Payment();
            PaymentResponse response = new PaymentResponse();

            when(paymentContext.refund(id)).thenReturn(payment);
            when(paymentMapper.toResponse(payment)).thenReturn(response);

            PaymentResponse result = paymentFacade.refund(id);

            assertThat(result).isSameAs(response);

            verify(paymentContext).refund(id);
            verify(paymentMapper).toResponse(payment);
        }
    }
}