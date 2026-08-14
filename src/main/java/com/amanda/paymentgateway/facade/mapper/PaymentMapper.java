package com.amanda.paymentgateway.facade.mapper;

import com.amanda.paymentgateway.dto.request.PaymentRequest;
import com.amanda.paymentgateway.dto.response.PaymentResponse;
import com.amanda.paymentgateway.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Payment toEntity(PaymentRequest request);

    PaymentResponse toResponse(Payment payment);
}
