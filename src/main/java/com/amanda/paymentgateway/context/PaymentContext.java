package com.amanda.paymentgateway.context;

import com.amanda.paymentgateway.entity.Payment;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface PaymentContext {

    Payment create(String idempotencyKey, Payment payment);

    Payment findById(UUID id);

    Payment authorize (UUID id);

    Payment cancel(UUID id);

    Payment decline(UUID id);

    Payment refund(UUID id);

}
