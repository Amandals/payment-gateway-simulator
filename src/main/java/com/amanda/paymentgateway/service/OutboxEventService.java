package com.amanda.paymentgateway.service;

import com.amanda.paymentgateway.entity.OutboxEvent;

import java.util.List;

public interface OutboxEventService {

    OutboxEvent save(OutboxEvent event);

    List<OutboxEvent> findPendingEvents();

    void markAsProcessed(OutboxEvent event);
}
