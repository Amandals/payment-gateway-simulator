package com.amanda.paymentgateway.concurrency;

import com.amanda.paymentgateway.config.PostgresTestConfig;
import com.amanda.paymentgateway.context.PaymentContext;
import com.amanda.paymentgateway.entity.Payment;
import com.amanda.paymentgateway.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(PostgresTestConfig.class)
class PaymentConcurrencyIntegrationTest {

    @Autowired
    private PaymentContext paymentContext;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void cleanDatabase() {
        paymentRepository.deleteAll();
    }

    @Test
    void shouldCreateOnlyOnePaymentForConcurrentRequests() throws Exception {

        String idempotencyKey = "concurrent-idempotency-key";

        Payment payment1 = createPayment();
        Payment payment2 = createPayment();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            CountDownLatch start = new CountDownLatch(1);

            var future1 = executor.submit(() -> {
                start.await();
                return paymentContext.create(idempotencyKey, payment1);
            });

            var future2 = executor.submit(() -> {
                start.await();
                return paymentContext.create(idempotencyKey, payment2);
            });

            start.countDown();

            Payment result1 = future1.get();
            Payment result2 = future2.get();

            assertThat(result1).isNotNull();
            assertThat(result2).isNotNull();
            assertThat(result1.getId()).isEqualTo(result2.getId());
            assertThat(result1.getIdempotencyKey()).isEqualTo(idempotencyKey);

            List<Payment> payments =
                    paymentRepository.findAll()
                            .stream()
                            .filter(payment ->
                                    idempotencyKey.equals(
                                            payment.getIdempotencyKey()
                                    ))
                            .toList();

            assertThat(payments).hasSize(1);

        } finally {
            executor.shutdown();
        }
    }

    private Payment createPayment() {
        Payment payment = new Payment();

        payment.setAmount(new BigDecimal("100.00"));

        payment.setCurrency("BRL");
        payment.setDescription("Concurrency test");

        return payment;
    }
}