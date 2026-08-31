package com.amanda.paymentgateway.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxPayloadSerializerTest {

    private final OutboxPayloadSerializer serializer =
            new OutboxPayloadSerializer(new ObjectMapper());

    @Test
    void shouldSerializePayloadAsJson() {

        PaymentPayload payload = new PaymentPayload("123", "AUTHORIZED");

        String result = serializer.serialize(payload);

        assertThat(result)
                .contains("\"paymentId\":\"123\"")
                .contains("\"status\":\"AUTHORIZED\"");
    }

    private record PaymentPayload(String paymentId, String status) {
    }
}