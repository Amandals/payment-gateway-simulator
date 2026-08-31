package com.amanda.paymentgateway.outbox.impl;

import com.amanda.paymentgateway.entity.OutboxEvent;
import com.amanda.paymentgateway.outbox.EventPublisher;
import com.amanda.paymentgateway.outbox.OutboxEventPublisher;
import com.amanda.paymentgateway.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisherImpl implements OutboxEventPublisher {
    private final OutboxEventService eventService;
    private final EventPublisher eventPublisher;

    public void publishPendingEvents() {
        eventService.findPendingEvents()
                .forEach(this::publish);
    }

    public void publish(OutboxEvent event) {
        eventPublisher.publish(event);
        eventService.markAsProcessed(event);
    }

}
