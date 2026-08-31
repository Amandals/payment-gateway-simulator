package com.amanda.paymentgateway.outbox;

import com.amanda.paymentgateway.entity.OutboxEvent;
import com.amanda.paymentgateway.enums.OutboxEventStatus;
import com.amanda.paymentgateway.enums.OutboxEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventFactoryTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private OutboxEventFactory factory;

    private static final Instant NOW =
            Instant.parse("2026-08-14T12:00:00Z");

    @Test
    void shouldCreatePendingOutboxEvent() {
        UUID aggregateId = UUID.randomUUID();

        OutboxEventType eventType = OutboxEventType.PAYMENT_CREATED;
        String payload = "{\"paymentId\":\"123\"}";

        when(clock.instant()).thenReturn(NOW);

        OutboxEvent result = factory.create(aggregateId, eventType, payload);

        assertThat(result.getAggregateId()).isEqualTo(aggregateId);
        assertThat(result.getEventType()).isEqualTo(eventType);
        assertThat(result.getPayload()).isEqualTo(payload);
        assertThat(result.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
        assertThat(result.getProcessedAt()).isNull();

        verify(clock).instant();
    }
}