package com.distributed.ledger.application.service;

import com.distributed.ledger.domain.event.TransactionCreatedEvent;
import com.distributed.ledger.domain.model.Account;
import com.distributed.ledger.domain.model.AccountId;
import com.distributed.ledger.domain.model.Transaction;
import com.distributed.ledger.domain.port.in.SendMoneyCommand;
import com.distributed.ledger.domain.port.in.SendMoneyUseCase;
import com.distributed.ledger.domain.port.out.LoadAccountPort;
import com.distributed.ledger.domain.port.out.SaveAccountPort;
import com.distributed.ledger.domain.port.out.SaveTransactionPort;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendMoneyService implements SendMoneyUseCase {

    // Constants - Magic Strings/Numbers Prevention
    private static final String IDEMPOTENCY_PREFIX = "txn_idempotency:";
    private static final String LOCK_PREFIX = "lock:account:";
    private static final long LOCK_WAIT_TIME = 2;
    private static final long LOCK_LEASE_TIME = 5;
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

    private final LoadAccountPort loadAccountPort;
    private final SaveAccountPort saveAccountPort;
    private final SaveTransactionPort saveTransactionPort;
    private final ApplicationEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final MeterRegistry meterRegistry;

    // Programatik Transaction Yönetimi
    private final TransactionTemplate transactionTemplate;

    @Override
    public boolean sendMoney(SendMoneyCommand command) {

        // 1. IDEMPOTENCY CHECK (First Line of Defense)
        String idempotencyKey = IDEMPOTENCY_PREFIX + command.reference();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(idempotencyKey))) {
            meterRegistry.counter("business.idempotency.hit").increment();
            log.warn("Idempotency hit for reference: {}", command.reference());
            return true;
        }

        // 2. DISTRIBUTED LOCK (Redisson)
        String lockKey = LOCK_PREFIX + command.fromAccountId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);

            if (!isLocked) {
                meterRegistry.counter("business.lock.failure").increment();
                log.warn("Could not acquire lock for account: {}", command.fromAccountId());
                throw new RuntimeException("System is busy, please try again later.");
            }

            // 3. TRANSACTIONAL BLOCK (Database Operations)
            // Sadece veritabanı işlemleri bu blokta.
            // Redis yazma işlemi dışarıya alındı.
            Boolean transactionResult = transactionTemplate.execute(status -> {
                log.info("Initiating transfer: {} from {} to {}", command.amount(), command.fromAccountId(), command.toAccountId());

                Account sourceAccount = loadAccountPort.loadAccount(AccountId.of(command.fromAccountId()));
                Account targetAccount = loadAccountPort.loadAccount(AccountId.of(command.toAccountId()));

                Transaction transaction = Transaction.createTransfer(
                        command.reference(),
                        AccountId.of(command.fromAccountId()),
                        AccountId.of(command.toAccountId()),
                        command.amount(),
                        "Transfer from " + command.fromAccountId()
                );

                sourceAccount.withdraw(command.amount());
                targetAccount.deposit(command.amount());
                transaction.complete();

                saveTransactionPort.saveTransaction(transaction);
                saveAccountPort.saveAccount(sourceAccount);
                saveAccountPort.saveAccount(targetAccount);


                // Event yayınlama transaction içinde olmalı (Outbox pattern için)
                eventPublisher.publishEvent(new TransactionCreatedEvent(transaction));

                return true;
            });

            // 4. IDEMPOTENCY MARK (Redis - Optimization)
            // DB Transaction başarıyla commit olduysa (result true ise), Redis'e yaz.
            // Bu sayede Redis network gecikmesi DB connection'ı meşgul etmez.
            if (Boolean.TRUE.equals(transactionResult)) {
                redisTemplate.opsForValue().set(idempotencyKey, "COMPLETED", IDEMPOTENCY_TTL);
                log.info("Transfer successful. Ref: {}", command.reference());
                return true;
            }

            return false;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock acquisition interrupted", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}