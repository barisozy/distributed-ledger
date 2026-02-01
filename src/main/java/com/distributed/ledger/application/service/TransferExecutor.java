package com.distributed.ledger.application.service;

import com.distributed.ledger.domain.event.TransactionCreatedEvent;
import com.distributed.ledger.domain.model.*;
import com.distributed.ledger.domain.port.in.SendMoneyCommand;
import com.distributed.ledger.domain.port.out.LoadAccountPort;
import com.distributed.ledger.domain.port.out.SaveAccountPort;
import com.distributed.ledger.domain.port.out.SaveLedgerEntryPort;
import com.distributed.ledger.domain.port.out.SaveTransactionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferExecutor {

    private final LoadAccountPort loadAccountPort;
    private final SaveAccountPort saveAccountPort;
    private final SaveTransactionPort saveTransactionPort;
    private final SaveLedgerEntryPort saveLedgerEntryPort;
    private final ApplicationEventPublisher eventPublisher;

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(SendMoneyCommand command) {
        Instant now = Instant.now();
        LocalDateTime transactionTime = LocalDateTime.ofInstant(now, ZoneId.of("UTC"));

        log.info("Initiating DB transaction for Ref: {}", command.reference());

        Account sourceAccount = loadAccountPort.loadAccount(AccountId.of(command.fromAccountId()));
        Account targetAccount = loadAccountPort.loadAccount(AccountId.of(command.toAccountId()));

        sourceAccount.withdraw(command.amount());
        targetAccount.deposit(command.amount());

        Transaction transaction = Transaction.createTransfer(
                command.reference(),
                AccountId.of(command.fromAccountId()),
                AccountId.of(command.toAccountId()),
                command.amount(),
                "Transfer Ref: " + command.reference(),
                transactionTime
        );
        transaction.complete(transactionTime);

        LedgerEntry debitEntry = LedgerEntry.create(
                TransactionId.of(transaction.getId()),
                sourceAccount.getId(),
                LedgerEntry.EntryType.DEBIT,
                command.amount(),
                sourceAccount.getBalance(),
                now
        );

        LedgerEntry creditEntry = LedgerEntry.create(
                TransactionId.of(transaction.getId()),
                targetAccount.getId(),
                LedgerEntry.EntryType.CREDIT,
                command.amount(),
                targetAccount.getBalance(),
                now
        );

        saveAccountPort.saveAccount(sourceAccount);
        saveAccountPort.saveAccount(targetAccount);
        saveTransactionPort.saveTransaction(transaction);
        saveLedgerEntryPort.saveAll(List.of(debitEntry, creditEntry));

        eventPublisher.publishEvent(new TransactionCreatedEvent(transaction));
    }
}