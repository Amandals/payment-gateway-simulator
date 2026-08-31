package com.amanda.paymentgateway.outbox.impl;

import com.amanda.paymentgateway.config.RabbitMqConfig;
import com.amanda.paymentgateway.entity.OutboxEvent;
import com.amanda.paymentgateway.outbox.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMqEventPublisherImpl implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(OutboxEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.PAYMENT_EXCHANGE,
                event.getEventType().getRoutingKey(),
                event.getPayload()
        );
    }
}
