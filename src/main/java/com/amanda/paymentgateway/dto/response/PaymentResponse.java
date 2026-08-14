package com.amanda.paymentgateway.dto.response;

import com.amanda.paymentgateway.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class PaymentResponse {
    private UUID id;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}