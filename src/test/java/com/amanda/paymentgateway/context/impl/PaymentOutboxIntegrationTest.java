package com.amanda.paymentgateway.context.impl;

import com.amanda.paymentgateway.config.PostgresTestConfig;
import com.amanda.paymentgateway.entity.OutboxEvent;
import com.amanda.paymentgateway.entity.Payment;
import com.amanda.paymentgateway.enums.OutboxEventStatus;
import com.amanda.paymentgateway.enums.OutboxEventType;
import com.amanda.paymentgateway.outbox.PaymentCreatedEvent;
import com.amanda.paymentgateway.repository.OutboxEventRepository;
import com.amanda.paymentgateway.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@Testcontainers
@SpringBootTest
@Import(PostgresTestConfig.class)
class PaymentOutboxIntegrationTest {

    @Autowired
    private PaymentCreationTransaction paymentCreationTransaction;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoSpyBean
    private OutboxEventRepository outboxEventRepositorySpy;

    @AfterEach
    void cleanDatabase() {
        outboxEventRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Test
    void shouldCreatePaymentAndOutboxEvent() throws JsonProcessingException {
        Payment payment = new Payment();

        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("BRL");
        payment.setDescription("Test payment");

        String idempotencyKey = "idempotency-123";

        Payment savedPayment = paymentCreationTransaction.create(idempotencyKey, payment);
        List<OutboxEvent> events = outboxEventRepository.findAll();

        OutboxEvent outboxEvent = events.get(0);
        PaymentCreatedEvent event = objectMapper.readValue(outboxEvent.getPayload(), PaymentCreatedEvent.class);

        assertThat(savedPayment.getId()).isNotNull();
        assertThat(paymentRepository.findById(savedPayment.getId())).isPresent();
        assertThat(outboxEventRepository.findAll()).hasSize(1);

        assertThat(outboxEvent.getAggregateId()).isEqualTo(savedPayment.getId());
        assertThat(outboxEvent.getEventType()).isEqualTo(OutboxEventType.PAYMENT_CREATED);
        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(outboxEvent.getPayload()).isNotBlank();
        assertThat(outboxEvent.getCreatedAt()).isNotNull();

        assertThat(event.paymentId()).isEqualTo(savedPayment.getId());
        assertThat(event.amount()).isEqualByComparingTo(savedPayment.getAmount());
        assertThat(event.currency()).isEqualTo(savedPayment.getCurrency());
        assertThat(event.status()).isEqualTo(savedPayment.getStatus());
    }

    @Test
    void shouldRollbackPaymentWhenOutboxCreationFails() {
        Payment payment = new Payment();

        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("BRL");
        payment.setDescription("Test payment");

        String idempotencyKey = "idempotency-123";

        doThrow(new RuntimeException("Outbox failure"))
                .when(outboxEventRepositorySpy)
                .save(any(OutboxEvent.class));

        assertThatThrownBy(() ->
                paymentCreationTransaction.create(idempotencyKey, payment))
                .isInstanceOf(RuntimeException.class);

        assertThat(paymentRepository.findById(payment.getId())).isEmpty();
        assertThat(outboxEventRepository.findAll()).isEmpty();
    }
}
