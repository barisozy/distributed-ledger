package com.distributed.ledger.domain.model;

import java.util.UUID;

public record TransactionId(UUID value) {
    public TransactionId {
        if (value == null) {
            throw new IllegalArgumentException("TransactionId cannot be null");
        }
    }

    public static TransactionId of(UUID value) {
        return new TransactionId(value);
    }

    public static TransactionId generate() {
        return new TransactionId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}