package com.amanda.paymentgateway.dto.response;

public record ErrorResponse(
        int status,
        String message
) {
}
