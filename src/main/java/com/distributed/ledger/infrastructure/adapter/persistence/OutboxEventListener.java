package com.distributed.ledger.infrastructure.adapter.persistence;

import com.distributed.ledger.domain.event.TransactionCreatedEvent;
import com.distributed.ledger.infrastructure.adapter.persistence.entity.OutboxEntity;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataOutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxEventListener {

    private final SpringDataOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @EventListener
    public void handle(TransactionCreatedEvent event) {
        try {
            OutboxEntity outbox = OutboxEntity.builder()
                    .id(UUID.randomUUID())
                    .aggregateType("TRANSACTION")
                    .aggregateId(event.getTransactionId().toString())
                    .type("TRANSACTION_CREATED")
                    .payload(objectMapper.writeValueAsString(event))
                    .createdAt(LocalDateTime.now())
                    .processed(false)
                    .build();

            outboxRepository.save(outbox);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing event payload", e);
        }
    }
}