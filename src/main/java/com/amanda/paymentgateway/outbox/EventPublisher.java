package com.amanda.paymentgateway.outbox;

import com.amanda.paymentgateway.entity.OutboxEvent;

public interface EventPublisher {

    void publish(OutboxEvent event);
}
