package com.amanda.paymentgateway.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Amount must be greater than zero"
    )
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(
            min = 3,
            max = 3,
            message = "Currency must have exactly 3 characters"
    )
    private String currency;

    @Size(
            max = 255,
            message = "Description must have at most 255 characters"
    )
    private String description;
}