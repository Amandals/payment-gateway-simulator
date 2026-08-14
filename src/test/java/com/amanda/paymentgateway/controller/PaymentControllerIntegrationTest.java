package com.amanda.paymentgateway.controller;

import com.amanda.paymentgateway.dto.request.PaymentRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentRequest validRequest() {
        PaymentRequest request = new PaymentRequest();

        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("BRL");
        request.setDescription("Integration test payment");

        return request;
    }

    private UUID createPayment() throws Exception {
        String response = mockMvc.perform(
                        post("/payments")
                                .header("Idempotency-Key", UUID.randomUUID().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest()))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        return UUID.fromString(json.get("id").asText());
    }

    @Nested
    class Create {

        @Test
        void shouldCreatePayment() throws Exception {
            mockMvc.perform(
                            post("/payments")
                                    .header("Idempotency-Key", UUID.randomUUID().toString())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest()))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.amount").value(100.00))
                    .andExpect(jsonPath("$.currency").value("BRL"))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.description")
                            .value("Integration test payment"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldFindCreatedPayment() throws Exception {
            UUID id = createPayment();

            mockMvc.perform(get("/payments/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.amount").value(100.00))
                    .andExpect(jsonPath("$.currency").value("BRL"))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.description")
                            .value("Integration test payment"));
        }

        @Test
        void shouldReturnNotFoundWhenPaymentDoesNotExist() throws Exception {
            UUID id = UUID.randomUUID();

            mockMvc.perform(get("/payments/{id}", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Authorize {

        @Test
        void shouldAuthorizePendingPayment() throws Exception {
            UUID id = createPayment();

            mockMvc.perform(post("/payments/{id}/authorize", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.status").value("AUTHORIZED"))
                    .andExpect(jsonPath("$.updatedAt").exists());
        }
    }

    @Nested
    class Cancel {

        @Test
        void shouldCancelPendingPayment() throws Exception {
            UUID id = createPayment();

            mockMvc.perform(post("/payments/{id}/cancel", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.status").value("CANCELLED"))
                    .andExpect(jsonPath("$.updatedAt").exists());
        }
    }

    @Nested
    class Decline {

        @Test
        void shouldDeclinePendingPayment() throws Exception {
            UUID id = createPayment();

            mockMvc.perform(post("/payments/{id}/decline", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.status").value("DECLINED"))
                    .andExpect(jsonPath("$.updatedAt").exists());
        }
    }

    @Nested
    class Refund {

        @Test
        void shouldRefundAuthorizedPayment() throws Exception {
            UUID id = createPayment();

            mockMvc.perform(post("/payments/{id}/authorize", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("AUTHORIZED"));

            mockMvc.perform(post("/payments/{id}/refund", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.status").value("REFUNDED"))
                    .andExpect(jsonPath("$.updatedAt").exists());
        }
    }

    @Nested
    class Validation {

        private void performInvalidRequest(PaymentRequest request) throws Exception {
            mockMvc.perform(
                            post("/payments")
                                    .header(
                                            "Idempotency-Key",
                                            UUID.randomUUID().toString()
                                    )
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(request)
                                    )
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldRejectNullAmount() throws Exception {
            PaymentRequest request = validRequest();
            request.setAmount(null);

            performInvalidRequest(request);
        }

        @Test
        void shouldRejectAmountLessThanMinimum() throws Exception {
            PaymentRequest request = validRequest();
            request.setAmount(new BigDecimal("0.00"));

            performInvalidRequest(request);
        }

        @Test
        void shouldRejectNegativeAmount() throws Exception {
            PaymentRequest request = validRequest();
            request.setAmount(new BigDecimal("-10.00"));

            performInvalidRequest(request);
        }

        @Test
        void shouldRejectNullCurrency() throws Exception {
            PaymentRequest request = validRequest();
            request.setCurrency(null);

            performInvalidRequest(request);
        }

        @Test
        void shouldRejectBlankCurrency() throws Exception {
            PaymentRequest request = validRequest();
            request.setCurrency("");

            performInvalidRequest(request);
        }

        @Test
        void shouldRejectCurrencyWithLessThanThreeCharacters() throws Exception {
            PaymentRequest request = validRequest();
            request.setCurrency("BR");

            performInvalidRequest(request);
        }

        @Test
        void shouldRejectCurrencyWithMoreThanThreeCharacters() throws Exception {
            PaymentRequest request = validRequest();
            request.setCurrency("BRLL");

            performInvalidRequest(request);
        }

        @Test
        void shouldRejectDescriptionWithMoreThan255Characters() throws Exception {
            PaymentRequest request = validRequest();
            request.setDescription("a".repeat(256));

            performInvalidRequest(request);
        }

        @Test
        void shouldAcceptNullDescription() throws Exception {
            PaymentRequest request = validRequest();
            request.setDescription(null);

            mockMvc.perform(
                            post("/payments")
                                    .header(
                                            "Idempotency-Key",
                                            UUID.randomUUID().toString()
                                    )
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(request)
                                    )
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.amount").value(100.00))
                    .andExpect(jsonPath("$.currency").value("BRL"))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.description").doesNotExist());
        }
    }
}