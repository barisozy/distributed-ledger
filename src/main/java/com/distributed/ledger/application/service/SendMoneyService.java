package com.distributed.ledger.application.service;

import com.distributed.ledger.domain.port.in.SendMoneyCommand;
import com.distributed.ledger.domain.port.in.SendMoneyUseCase;
import com.distributed.ledger.domain.port.out.CachePort;
import com.distributed.ledger.domain.port.out.DistributedLockPort;
import com.distributed.ledger.domain.port.out.SaveTransactionPort;
import com.distributed.ledger.application.service.TransferExecutor;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendMoneyService implements SendMoneyUseCase {

    private static final String IDEMPOTENCY_PREFIX = "txn_lock:";
    private static final String PROCESSED_CACHE_PREFIX = "txn_processed:";
    private static final Duration PROCESSED_CACHE_TTL = Duration.ofHours(24);

    private final TransferExecutor transferExecutor;
    private final SaveTransactionPort saveTransactionPort;
    private final DistributedLockPort distributedLockPort;
    private final CachePort cachePort;
    private final MeterRegistry meterRegistry;

    @Override
    public boolean sendMoney(SendMoneyCommand command) {
        String lockKey = IDEMPOTENCY_PREFIX + command.reference();

        if (checkIdempotency(command.reference())) {
            return true;
        }

        distributedLockPort.executeInLock(lockKey, () -> {
            if (checkIdempotency(command.reference())) {
                return;
            }

            transferExecutor.execute(command);
            markAsProcessedInCache(command.reference());

            log.info("Transfer completed successfully. Ref: {}", command.reference());
        });

        return true;
    }

    private boolean checkIdempotency(String reference) {
        String cacheKey = PROCESSED_CACHE_PREFIX + reference;

        if (cachePort.exists(cacheKey)) {
            log.info("Idempotency hit (CACHE). Ref: {}", reference);
            meterRegistry.counter("business.idempotency.hit", "source", "cache").increment();
            return true;
        }

        if (saveTransactionPort.existsByReference(reference)) {
            log.info("Idempotency hit (DB). Ref: {}", reference);
            meterRegistry.counter("business.idempotency.hit", "source", "db").increment();
            markAsProcessedInCache(reference);
            return true;
        }

        return false;
    }

    private void markAsProcessedInCache(String reference) {
        try {
            String cacheKey = PROCESSED_CACHE_PREFIX + reference;
            cachePort.put(cacheKey, "COMPLETED", PROCESSED_CACHE_TTL);
        } catch (Exception e) {
            log.warn("Failed to update idempotency cache. Ref: {}", reference, e);
        }
    }
}