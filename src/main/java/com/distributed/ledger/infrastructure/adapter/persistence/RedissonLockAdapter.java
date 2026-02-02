package com.distributed.ledger.infrastructure.adapter.persistence;

import com.distributed.ledger.domain.port.out.DistributedLockPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonLockAdapter implements DistributedLockPort {

    private final RedissonClient redissonClient;
    private static final long WAIT_TIME = 0;
    private static final long LEASE_TIME = 10;

    @Override
    public void executeInLock(String key, Runnable action) {
        RLock lock = redissonClient.getLock(key);
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);

            if (!isLocked) {
                log.warn("Concurrent processing attempt rejected for key: {}", key);
                throw new OptimisticLockingFailureException("Transaction is currently being processed by another thread.");
            }

            action.run();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock acquisition interrupted", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}