package com.distributed.ledger.infrastructure.scheduler;

import com.distributed.ledger.infrastructure.adapter.persistence.entity.OutboxEntity;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataOutboxRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {

    private final SpringDataOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MeterRegistry meterRegistry;

    @Value("${ledger.outbox.batch-size:50}")
    private int batchSize;

    @Value("${ledger.outbox.kafka-topic:transaction-events}")
    private String kafkaTopic;

    @Value("${ledger.outbox.max-retries:5}")
    private int maxRetries;

    @Value("${ledger.outbox.retention-days:7}")
    private int retentionDays;

    @Value("${ledger.outbox.publish-timeout-sec:2}")
    private int publishTimeoutSec;

    @Scheduled(fixedDelayString = "${ledger.outbox.polling-interval-ms:2000}")
    @Transactional
    public void processOutbox() {
        Timer.Sample sample = Timer.start(meterRegistry);

        List<OutboxEntity> events = outboxRepository.findBatchToProcess(batchSize);

        if (events.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox events sequentially.", events.size());

        Set<String> failedAggregates = new HashSet<>();
        int successCount = 0;

        for (OutboxEntity event : events) {
            if (failedAggregates.contains(event.getAggregateId())) {
                log.warn("Skipping event {} for aggregate {} due to previous failure in batch.",
                        event.getId(), event.getAggregateId());
                continue;
            }

            try {
                processSingleEvent(event);
                successCount++;
            } catch (Exception e) {
                failedAggregates.add(event.getAggregateId());
                handleFailure(event, e);
            }
        }

        sample.stop(meterRegistry.timer("outbox.processing.duration"));
        if (successCount > 0) {
            log.info("Batch processing completed. Success: {}, Failed Aggregates: {}",
                    successCount, failedAggregates.size());
        }
    }

    private void processSingleEvent(OutboxEntity event) throws InterruptedException, ExecutionException, TimeoutException {
        SendResult<String, String> result = kafkaTemplate.send(kafkaTopic, event.getAggregateId(), event.getPayload())
                .get(publishTimeoutSec, TimeUnit.SECONDS);

        markAsProcessed(result, event);
    }

    private void markAsProcessed(SendResult<String, String> result, OutboxEntity event) {
        event.setProcessed(true);
        event.setUpdatedAt(LocalDateTime.now());
        event.setErrorMessage(null);
        outboxRepository.save(event);

        meterRegistry.counter("outbox.events.published", "status", "success").increment();
    }

    private void handleFailure(OutboxEntity event, Exception ex) {
        try {
            int currentRetries = event.getRetryCount() + 1;
            event.setRetryCount(currentRetries);
            event.setUpdatedAt(LocalDateTime.now());

            String errorMsg = ex.getMessage() != null ? ex.getMessage() : ex.toString();
            event.setErrorMessage(errorMsg.substring(0, Math.min(errorMsg.length(), 1000)));

            if (currentRetries >= maxRetries) {
                log.error("POISON PILL DETECTED: Event ID {} reached max retries ({}). Marking as failed to unblock queue.",
                        event.getId(), maxRetries);

                event.setProcessed(true);
                meterRegistry.counter("outbox.events.dead_letter").increment();
            } else {
                log.warn("Event ID {} failed (Attempt {}/{}). Will retry next cycle. Error: {}",
                        event.getId(), currentRetries, maxRetries, ex.getMessage());
            }

            outboxRepository.save(event);

        } catch (Exception dbEx) {
            log.error("CRITICAL: Failed to update failure status for event {}", event.getId(), dbEx);
        }
    }

    @Scheduled(cron = "${ledger.outbox.cleanup-cron:0 0 3 * * *}")
    @Transactional
    public void cleanupProcessedEvents() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);
        log.info("Starting outbox cleanup for events older than {}", threshold);

        int deletedCount = outboxRepository.deleteByProcessedTrueAndUpdatedAtBefore(threshold);

        log.info("Outbox cleanup completed. Deleted {} events.", deletedCount);
        meterRegistry.counter("outbox.cleanup.deleted").increment(deletedCount);
    }
}