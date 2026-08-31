package com.amanda.paymentgateway.outbox;

import com.amanda.paymentgateway.config.RabbitMqConfig;
import com.amanda.paymentgateway.entity.OutboxEvent;
import com.amanda.paymentgateway.enums.OutboxEventType;
import com.amanda.paymentgateway.outbox.impl.RabbitMqEventPublisherImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMqEventPublisherImplTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMqEventPublisherImpl eventPublisher;

    @Test
    void shouldPublishEventToRabbitMq() {
        OutboxEvent event = new OutboxEvent();

        event.setEventType(OutboxEventType.PAYMENT_CREATED);
        event.setPayload("{\"paymentId\":\"123\"}");

        eventPublisher.publish(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMqConfig.PAYMENT_EXCHANGE,
                "payment.created",
                "{\"paymentId\":\"123\"}"
        );
    }
}