package com.amanda.paymentgateway.outbox;

import com.amanda.paymentgateway.entity.OutboxEvent;
import com.amanda.paymentgateway.enums.OutboxEventStatus;
import com.amanda.paymentgateway.enums.OutboxEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxEventFactory {

    private final Clock clock;

    public OutboxEvent create(UUID aggregateId, OutboxEventType eventType, String payload) {
        Instant now = clock.instant();

        OutboxEvent event = new OutboxEvent();

        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(payload);
        event.setStatus(OutboxEventStatus.PENDING);
        event.setCreatedAt(now);

        return event;
    }
}