package com.amanda.paymentgateway.service;

import com.amanda.paymentgateway.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentService {

    Payment save(Payment payment);

    Payment findById(UUID id);

    Optional<Payment> findByIdempotencyKey(String idempotencyKeyKey);

    Payment saveAndFlush(Payment payment);
}
