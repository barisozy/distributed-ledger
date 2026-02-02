package com.distributed.ledger.integration;

import com.distributed.ledger.domain.model.AccountStatus;
import com.distributed.ledger.domain.model.Money;
import com.distributed.ledger.domain.port.in.SendMoneyCommand;
import com.distributed.ledger.domain.port.in.SendMoneyUseCase;
import com.distributed.ledger.infrastructure.adapter.persistence.entity.AccountEntity;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataAccountRepository;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataLedgerEntryRepository;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTransferIntegrationTest extends IntegrationTestBase {

    @Autowired
    private SendMoneyUseCase sendMoneyUseCase;

    @Autowired
    private SpringDataAccountRepository accountRepository;

    @Autowired
    private SpringDataTransactionRepository transactionRepository;

    @Autowired
    private SpringDataLedgerEntryRepository ledgerEntryRepository;

    private UUID aliceId;
    private UUID bobId;

    @BeforeEach
    void setupData() {
        ledgerEntryRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();

        aliceId = UUID.randomUUID();
        bobId = UUID.randomUUID();

        createAccount(aliceId, "TR-ALICE", "Alice", new BigDecimal("1000.00"));
        createAccount(bobId, "TR-BOB", "Bob", new BigDecimal("0.00"));
    }

    @Test
    void shouldTransferMoneySuccessfully() {
        SendMoneyCommand command = new SendMoneyCommand(
                aliceId, bobId, Money.of(new BigDecimal("100.00"), "TRY"), "REF-001"
        );

        boolean result = sendMoneyUseCase.sendMoney(command);

        assertThat(result).isTrue();

        AccountEntity alice = accountRepository.findById(aliceId).orElseThrow();
        AccountEntity bob = accountRepository.findById(bobId).orElseThrow();

        assertThat(alice.getBalance()).isEqualByComparingTo("900.00");
        assertThat(bob.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldHandleConcurrentTransfersWithOptimisticLocking() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    SendMoneyCommand command = new SendMoneyCommand(
                            aliceId, bobId, Money.of(new BigDecimal("10.00"), "TRY"), "CONCURRENT-REF-" + index
                    );
                    if (sendMoneyUseCase.sendMoney(command)) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.out.println("Transaction failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        AccountEntity alice = accountRepository.findById(aliceId).orElseThrow();
        AccountEntity bob = accountRepository.findById(bobId).orElseThrow();

        System.out.println("Success Count: " + successCount.get());
        System.out.println("Alice Balance: " + alice.getBalance());

        BigDecimal totalTransferred = new BigDecimal("10.00").multiply(new BigDecimal(successCount.get()));
        BigDecimal expectedAliceBalance = new BigDecimal("1000.00").subtract(totalTransferred);

        assertThat(alice.getBalance()).isEqualByComparingTo(expectedAliceBalance);
        assertThat(bob.getBalance()).isEqualByComparingTo(totalTransferred);
    }

    private void createAccount(UUID id, String number, String name, BigDecimal balance) {
        AccountEntity entity = new AccountEntity();
        entity.setId(id);
        entity.setAccountNumber(number);
        entity.setAccountNumberHash("TEST-HASH-" + number);
        entity.setAccountName(name);
        entity.setBalance(balance);
        entity.setCurrency("TRY");
        entity.setStatus(AccountStatus.ACTIVE);
        entity.setCreatedAt(LocalDateTime.now());
        accountRepository.save(entity);
    }
}