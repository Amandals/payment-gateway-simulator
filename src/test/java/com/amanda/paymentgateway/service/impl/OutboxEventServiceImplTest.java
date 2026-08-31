package com.amanda.paymentgateway.service.impl;

import com.amanda.paymentgateway.entity.OutboxEvent;
import com.amanda.paymentgateway.enums.OutboxEventStatus;
import com.amanda.paymentgateway.repository.OutboxEventRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventServiceImplTest {

    @Mock
    private OutboxEventRepository repository;

    @InjectMocks
    private OutboxEventServiceImpl service;

    @Mock
    private Clock clock;

    @Nested
    class Save {

        @Test
        void shouldSaveOutboxEvent() {
            OutboxEvent event = new OutboxEvent();
            OutboxEvent savedEvent = new OutboxEvent();

            when(repository.save(event)).thenReturn(savedEvent);

            OutboxEvent result = service.save(event);

            assertThat(result).isSameAs(savedEvent);

            verify(repository).save(event);
        }
    }

    @Nested
    class FindPendingEvents {

        @Test
        void shouldReturnPendingEvents() {
            List<OutboxEvent> events = List.of(new OutboxEvent(), new OutboxEvent());

            when(repository.findByStatus(OutboxEventStatus.PENDING)).thenReturn(events);

            List<OutboxEvent> result = service.findPendingEvents();

            assertThat(result).containsExactlyElementsOf(events);

            verify(repository).findByStatus(OutboxEventStatus.PENDING);
        }

        @Test
        void shouldReturnEmptyListWhenThereAreNoPendingEvents() {
            when(repository.findByStatus(OutboxEventStatus.PENDING)).thenReturn(List.of());

            List<OutboxEvent> result = service.findPendingEvents();

            assertThat(result).isEmpty();

            verify(repository).findByStatus(OutboxEventStatus.PENDING);
        }
    }

    @Nested
    class MarkAsProcessed {

        @Test
        void shouldMarkEventAsProcessed() {
            OutboxEvent event = new OutboxEvent();

            Instant processedAt =
                    Instant.parse("2026-08-18T12:00:00Z");

            when(clock.instant()).thenReturn(processedAt);

            service.markAsProcessed(event);

            assertThat(event.getStatus())
                    .isEqualTo(OutboxEventStatus.PROCESSED);

            assertThat(event.getProcessedAt())
                    .isEqualTo(processedAt);

            verify(clock).instant();
            verify(repository).save(event);
        }

    }
}
