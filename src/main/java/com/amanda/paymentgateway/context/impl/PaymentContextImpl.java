package com.amanda.paymentgateway.context.impl;

import com.amanda.paymentgateway.context.PaymentContext;
import com.amanda.paymentgateway.entity.Payment;
import com.amanda.paymentgateway.enums.PaymentStatus;
import com.amanda.paymentgateway.exception.BusinessException;
import com.amanda.paymentgateway.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentContextImpl implements PaymentContext {

    private final PaymentCreationTransaction paymentCreationTransaction;
    private final PaymentService paymentService;
    private final Clock clock;

    @Override
    @Transactional
    public Payment create(String idempotencyKey, Payment payment) {
        return paymentService.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> createPayment(idempotencyKey, payment));
    }

    private Payment createPayment(String idempotencyKey, Payment payment) {
        try {
            return paymentCreationTransaction.create(idempotencyKey, payment);
        } catch (DataIntegrityViolationException exception) {
            return paymentService.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> exception);
        }
    }

    @Override
    public Payment findById(UUID id) {
        return paymentService.findById(id);
    }

    @Override
    @Transactional
    public Payment authorize(UUID id) {
        Payment payment = findById(id);

        validateAuthorization(payment);

        payment.setStatus(PaymentStatus.AUTHORIZED);
        payment.setUpdatedAt(clock.instant());

        return paymentService.save(payment);
    }

    @Override
    @Transactional
    public Payment cancel(UUID id) {
        Payment payment = findById(id);

        validateCancellation(payment);

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setUpdatedAt(clock.instant());

        return paymentService.save(payment);
    }

    @Override
    @Transactional
    public Payment decline(UUID id) {
        Payment payment = findById(id);
        validateDecline(payment);

        payment.setStatus(PaymentStatus.DECLINED);
        payment.setUpdatedAt(clock.instant());

        return paymentService.save(payment);
    }

    @Override
    @Transactional
    public Payment refund(UUID id) {
        Payment payment = findById(id);
        validateRefund(payment);

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(clock.instant());

        return paymentService.save(payment);
    }

    private void validateAuthorization(Payment payment) {
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(
                    BusinessException.PAYMENT_NOT_AUTHORIZED
            );
        }
    }

    private void validateCancellation(Payment payment) {
        if (payment.getStatus() != PaymentStatus.PENDING &&
                payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new BusinessException(
                    BusinessException.PAYMENT_NOT_CANCELLABLE
            );
        }
    }

    private void validateDecline(Payment payment) {
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(
                    BusinessException.PAYMENT_NOT_DECLINABLE
            );
        }
    }

    private void validateRefund(Payment payment) {
        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new BusinessException(
                    BusinessException.PAYMENT_NOT_REFUNDABLE
            );
        }
    }
}
