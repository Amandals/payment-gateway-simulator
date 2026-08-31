package com.amanda.paymentgateway.outbox;

import com.amanda.paymentgateway.entity.OutboxEvent;
import com.amanda.paymentgateway.outbox.impl.OutboxEventPublisherImpl;
import com.amanda.paymentgateway.service.OutboxEventService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherImplTest {

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private OutboxEventPublisherImpl outboxEventPublisherImpl;

    @Nested
    class PublishPendingEvents {

        @Test
        void shouldPublishAllPendingEvents() {
            OutboxEvent firstEvent = new OutboxEvent();
            OutboxEvent secondEvent = new OutboxEvent();

            when(outboxEventService.findPendingEvents())
                    .thenReturn(List.of(firstEvent, secondEvent));

            outboxEventPublisherImpl.publishPendingEvents();

            verify(eventPublisher).publish(firstEvent);
            verify(eventPublisher).publish(secondEvent);
            verify(outboxEventService).markAsProcessed(firstEvent);
            verify(outboxEventService).markAsProcessed(secondEvent);
        }

        @Test
        void shouldDoNothingWhenThereAreNoPendingEvents() {
            when(outboxEventService.findPendingEvents())
                    .thenReturn(List.of());

            outboxEventPublisherImpl.publishPendingEvents();

            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    class Publish {

        @Test
        void shouldPublishEventAndMarkAsProcessed() {
            OutboxEvent event = new OutboxEvent();

            outboxEventPublisherImpl.publish(event);

            verify(eventPublisher).publish(event);
            verify(outboxEventService).markAsProcessed(event);
        }

        @Test
        void shouldNotMarkEventAsProcessedWhenPublicationFails() {
            OutboxEvent event = new OutboxEvent();

            doThrow(new RuntimeException("RabbitMQ unavailable"))
                    .when(eventPublisher)
                    .publish(event);

            assertThatThrownBy(() ->
                    outboxEventPublisherImpl.publish(event)
            )
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("RabbitMQ unavailable");

            verify(eventPublisher).publish(event);
            verify(outboxEventService, never())
                    .markAsProcessed(event);
        }
    }
}
