package com.amanda.paymentgateway.controller;

import com.amanda.paymentgateway.dto.request.PaymentRequest;
import com.amanda.paymentgateway.dto.response.PaymentResponse;
import com.amanda.paymentgateway.facade.PaymentFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(
        name = "Payments",
        description = "Operations for creating and managing payments"
)
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @Operation(
            summary = "Create a payment",
            description = "Creates a new payment with PENDING status."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {
        return paymentFacade.create(idempotencyKey, request);
    }

    @Operation(
            summary = "Find a payment",
            description = "Returns a payment by its UUID."
    )
    @GetMapping("/{id}")
    public PaymentResponse findById(@PathVariable UUID id) {
        return paymentFacade.findById(id);
    }

    @Operation(
            summary = "Authorize a payment",
            description = "Changes a PENDING payment to AUTHORIZED."
    )
    @PostMapping("/{id}/authorize")
    public PaymentResponse authorize(@PathVariable UUID id) {
        return paymentFacade.authorize(id);
    }

    @Operation(
            summary = "Cancel a payment",
            description = "Cancels a PENDING or AUTHORIZED payment."
    )
    @PostMapping("/{id}/cancel")
    public PaymentResponse cancel(@PathVariable UUID id) {
        return paymentFacade.cancel(id);
    }

    @Operation(
            summary = "Decline a payment",
            description = "Declines a PENDING payment."
    )
    @PostMapping("/{id}/decline")
    public PaymentResponse decline(@PathVariable UUID id) {
        return paymentFacade.decline(id);
    }

    @Operation(
            summary = "Refund a payment",
            description = "Refunds an AUTHORIZED payment."
    )
    @PostMapping("/{id}/refund")
    public PaymentResponse refund(@PathVariable UUID id) {
        return paymentFacade.refund(id);
    }
}
