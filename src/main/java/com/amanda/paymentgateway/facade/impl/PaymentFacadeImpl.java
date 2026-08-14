package com.amanda.paymentgateway.facade.impl;

import com.amanda.paymentgateway.context.PaymentContext;

import com.amanda.paymentgateway.dto.request.PaymentRequest;
import com.amanda.paymentgateway.dto.response.PaymentResponse;
import com.amanda.paymentgateway.entity.Payment;
import com.amanda.paymentgateway.facade.PaymentFacade;
import com.amanda.paymentgateway.facade.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentFacadeImpl implements PaymentFacade {

    private final PaymentMapper paymentMapper;
    private final PaymentContext paymentContext;

    @Override
    public PaymentResponse create(String idempotencyKey, PaymentRequest request) {
        Payment payment = paymentMapper.toEntity(request);
        Payment createdPayment = paymentContext.create(idempotencyKey, payment);
        return paymentMapper.toResponse(createdPayment);
    }

    @Override
    public PaymentResponse findById(UUID id){
        Payment payment = paymentContext.findById(id);
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse authorize(UUID id){
        Payment payment = paymentContext.authorize(id);
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse cancel(UUID id){
        Payment payment = paymentContext.cancel(id);
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse decline(UUID id) {
        Payment payment = paymentContext.decline(id);
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse refund(UUID id) {
        Payment payment = paymentContext.refund(id);
        return paymentMapper.toResponse(payment);
    }
}
