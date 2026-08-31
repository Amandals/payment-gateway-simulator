package com.amanda.paymentgateway.enums;

public enum OutboxEventType {

    PAYMENT_CREATED("payment.created"),
    PAYMENT_AUTHORIZED("payment.authorized"),
    PAYMENT_DECLINED("payment.declined"),
    PAYMENT_CANCELLED("payment.cancelled"),
    PAYMENT_REFUNDED("payment.refunded");

    private final String routingKey;

    OutboxEventType(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getRoutingKey() {
        return routingKey;
    }
}