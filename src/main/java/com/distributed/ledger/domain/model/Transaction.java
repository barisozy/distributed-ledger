package com.distributed.ledger.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private final UUID id;
    private final String reference;
    private final AccountId fromAccountId;
    private final AccountId toAccountId;
    private final Money amount;
    private final TransactionType type;
    private TransactionStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String description;

    public Transaction(UUID id, String reference, AccountId fromAccountId, AccountId toAccountId,
                       Money amount, TransactionType type, TransactionStatus status,
                       LocalDateTime createdAt, String description) {
        this.id = id;
        this.reference = reference;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.description = description;
    }

    public static Transaction with(UUID id, String reference, AccountId fromAccountId, AccountId toAccountId,
                                   Money amount, TransactionType type, TransactionStatus status,
                                   LocalDateTime createdAt, LocalDateTime completedAt, String description) {
        Transaction transaction = new Transaction(
                id,
                reference,
                fromAccountId,
                toAccountId,
                amount,
                type,
                status,
                createdAt,
                description
        );
        transaction.completedAt = completedAt;
        return transaction;
    }

    public static Transaction createTransfer(String reference, AccountId from, AccountId to, Money amount, String description, LocalDateTime createdAt) {
        return new Transaction(
                UUID.randomUUID(),
                reference,
                from,
                to,
                amount,
                TransactionType.TRANSFER,
                TransactionStatus.PENDING,
                createdAt,
                description
        );
    }

    public void complete(LocalDateTime completedAt) {
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = completedAt;
    }

    public void fail(LocalDateTime completedAt) {
        this.status = TransactionStatus.FAILED;
        this.completedAt = completedAt;
    }

    public UUID getId() { return id; }
    public String getReference() { return reference; }
    public AccountId getFromAccountId() { return fromAccountId; }
    public AccountId getToAccountId() { return toAccountId; }
    public Money getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public String getDescription() { return description; }
}