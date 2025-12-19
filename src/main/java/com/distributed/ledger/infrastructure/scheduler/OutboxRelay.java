package com.distributed.ledger.infrastructure.scheduler;

import com.distributed.ledger.infrastructure.adapter.persistence.entity.OutboxEntity;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataOutboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {

    private final SpringDataOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void processOutbox() {
        List<OutboxEntity> events = outboxRepository.findByProcessedFalseOrderByIdAsc(PageRequest.of(0, 50));

        for (OutboxEntity event : events) {
            try {
                kafkaTemplate.send("transaction-events", event.getAggregateId(), event.getPayload()).get();
                event.setProcessed(true);
                outboxRepository.save(event);

            } catch (Exception e) {
                log.error("Could not publish event: {}", event.getId(), e);
            }
        }
    }
}