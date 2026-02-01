package com.distributed.ledger.domain.model;

import java.time.Instant;
import java.util.UUID;

public record LedgerEntry(
        UUID id,
        TransactionId transactionId,
        AccountId accountId,
        EntryType type,
        Money amount,
        Money balanceAfter,
        Instant createdAt
) {
    public enum EntryType {
        DEBIT,
        CREDIT
    }

    public static LedgerEntry create(TransactionId txId, AccountId accId, EntryType type, Money amount, Money balanceAfter, Instant createdAt) {
        return new LedgerEntry(
                UUID.randomUUID(),
                txId,
                accId,
                type,
                amount,
                balanceAfter,
                createdAt
        );
    }
}