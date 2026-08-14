package com.amanda.paymentgateway.service.impl;

import com.amanda.paymentgateway.entity.Payment;
import com.amanda.paymentgateway.exception.BusinessException;
import com.amanda.paymentgateway.repository.PaymentRepository;
import com.amanda.paymentgateway.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findById(UUID id) {
        return paymentRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                    BusinessException.PAYMENT_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return paymentRepository
                .findByIdempotencyKey(idempotencyKey);
    }

    @Override
    @Transactional
    public Payment saveAndFlush(Payment payment) {
        return paymentRepository.saveAndFlush(payment);
    }
}