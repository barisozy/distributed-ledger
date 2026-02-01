package com.distributed.ledger.application.service;

import com.distributed.ledger.domain.model.*;
import com.distributed.ledger.domain.port.in.SendMoneyCommand;
import com.distributed.ledger.domain.port.in.SendMoneyUseCase;
import com.distributed.ledger.domain.port.out.LoadAccountPort;
import com.distributed.ledger.domain.port.out.SaveAccountPort;
import com.distributed.ledger.domain.port.out.SaveLedgerEntryPort;
import com.distributed.ledger.domain.port.out.SaveTransactionPort;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendMoneyService implements SendMoneyUseCase {

    private static final String IDEMPOTENCY_PREFIX = "txn_lock:";
    private static final String PROCESSED_CACHE_PREFIX = "txn_processed:";
    private static final long LOCK_TTL = 10;
    private static final Duration PROCESSED_CACHE_TTL = Duration.ofHours(24);

    private final TransferExecutor transferExecutor;
    private final SaveTransactionPort saveTransactionPort;
    private final RedissonClient redissonClient;
    private final MeterRegistry meterRegistry;

    @Override
    public boolean sendMoney(SendMoneyCommand command) {
        String lockKey = IDEMPOTENCY_PREFIX + command.reference();

        if (checkIdempotency(command.reference())) {
            return true;
        }

        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(0, LOCK_TTL, TimeUnit.SECONDS);

            if (!isLocked) {
                log.warn("Concurrent processing attempt rejected for ref: {}", command.reference());
                throw new OptimisticLockingFailureException("Transaction is currently being processed.");
            }

            try {
                if (checkIdempotency(command.reference())) {
                    return true;
                }

                transferExecutor.execute(command);
                markAsProcessedInCache(command.reference());

                log.info("Transfer completed successfully. Ref: {}", command.reference());
                return true;

            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock acquisition interrupted for ref: {}", command.reference(), e);
            throw new RuntimeException("Lock acquisition interrupted", e);
        } catch (Exception e) {
            log.error("Transfer failed for ref: {}", command.reference(), e);
            throw e;
        }
    }

    private boolean checkIdempotency(String reference) {
        String cacheKey = PROCESSED_CACHE_PREFIX + reference;
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);

        if (bucket.isExists()) {
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
            redissonClient.getBucket(cacheKey).set("COMPLETED", PROCESSED_CACHE_TTL);
        } catch (Exception e) {
            log.warn("Failed to update idempotency cache for ref: {}", reference, e);
        }
    }
}