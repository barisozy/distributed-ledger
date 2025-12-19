package com.distributed.ledger.domain.model;

public enum AccountStatus {
    ACTIVE,
    FROZEN,
    CLOSED;

    public boolean canTransact() {
        return this == ACTIVE;
    }
}