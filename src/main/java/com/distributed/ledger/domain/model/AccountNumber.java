package com.distributed.ledger.domain.model;

public record AccountNumber(String value) {
    public AccountNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Account number cannot be empty");
        }
    }

    public static AccountNumber of(String value) {
        return new AccountNumber(value);
    }

    @Override
    public String toString() {
        if (value.length() < 4) return "****";
        return "****" + value.substring(value.length() - 4);
    }
}