package com.distributed.ledger.domain.model;

import java.util.UUID;

public record AccountId(UUID value) {
    public AccountId {
        if (value == null) {
            throw new IllegalArgumentException("AccountId cannot be null");
        }
    }

    public static AccountId of(UUID value) {
        return new AccountId(value);
    }

    public static AccountId generate() {
        return new AccountId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}