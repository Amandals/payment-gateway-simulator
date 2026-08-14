package com.amanda.paymentgateway.facade;

import com.amanda.paymentgateway.dto.request.PaymentRequest;
import com.amanda.paymentgateway.dto.response.PaymentResponse;

import java.util.UUID;

public interface PaymentFacade {

    PaymentResponse create(String idempotencyKey, PaymentRequest request);

    PaymentResponse findById(UUID id);

    PaymentResponse authorize (UUID id);

    PaymentResponse cancel(UUID id);

    PaymentResponse decline(UUID id);

    PaymentResponse refund(UUID id);

}
