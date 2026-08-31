package com.amanda.paymentgateway.outbox;

public interface OutboxEventPublisher {

    void publishPendingEvents();

}
