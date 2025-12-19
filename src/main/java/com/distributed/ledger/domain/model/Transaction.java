package com.distributed.ledger.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private final UUID id;
    private final String reference; // Unique external reference (Idempotency için)
    private final AccountId fromAccountId; // Nullable (Deposit ise)
    private final AccountId toAccountId;   // Nullable (Withdrawal ise)
    private final Money amount;
    private final TransactionType type;
    private TransactionStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String description;

    // Constructor'ı private tutup Builder pattern kullanmak daha temiz olur ama
    // şimdilik basit bir constructor ile ilerleyelim.
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

    public static Transaction createTransfer(String reference, AccountId from, AccountId to, Money amount, String description) {
        return new Transaction(
                UUID.randomUUID(),
                reference,
                from,
                to,
                amount,
                TransactionType.TRANSFER,
                TransactionStatus.PENDING,
                LocalDateTime.now(),
                description
        );
    }

    public void complete() {
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = TransactionStatus.FAILED;
        this.completedAt = LocalDateTime.now();
    }

    // Getters
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