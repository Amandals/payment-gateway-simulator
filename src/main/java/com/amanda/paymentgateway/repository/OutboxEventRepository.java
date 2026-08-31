package com.amanda.paymentgateway.repository;

import com.amanda.paymentgateway.entity.OutboxEvent;
import com.amanda.paymentgateway.enums.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatus(OutboxEventStatus status);

}
