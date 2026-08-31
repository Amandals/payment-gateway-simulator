package com.amanda.paymentgateway.service.impl;

import com.amanda.paymentgateway.entity.OutboxEvent;
import com.amanda.paymentgateway.enums.OutboxEventStatus;
import com.amanda.paymentgateway.repository.OutboxEventRepository;
import com.amanda.paymentgateway.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxEventServiceImpl implements OutboxEventService {

    private final OutboxEventRepository repository;
    private final Clock clock;

    @Override
    @Transactional
    public OutboxEvent save(OutboxEvent event) {
        return repository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEvent> findPendingEvents() {
        return repository.findByStatus(OutboxEventStatus.PENDING);
    }

    @Override
    public void markAsProcessed(OutboxEvent event) {
        event.setStatus(OutboxEventStatus.PROCESSED);
        event.setProcessedAt(clock.instant());

        repository.save(event);
    }
}
