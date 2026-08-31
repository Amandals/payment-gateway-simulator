package com.amanda.paymentgateway.exception;

public class BusinessException extends RuntimeException {
    //TODO - Refactor BusinessException to use structured error codes.
    public static final String PAYMENT_NOT_FOUND = "Payment not found";
    public static final String PAYMENT_NOT_AUTHORIZED = "Payment not authorized";
    public static final String PAYMENT_NOT_CANCELLABLE = "Payment cannot be cancelled";
    public static final String PAYMENT_NOT_DECLINABLE = "Payment cannot be declined";
    public static final String PAYMENT_NOT_REFUNDABLE = "Payment cannot be refunded";

    public BusinessException(String message) {
        super(message);
    }
}