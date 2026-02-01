package com.distributed.ledger.domain.port.in;

import com.distributed.ledger.domain.model.Money;
import java.util.UUID;

public record SendMoneyCommand(
        UUID fromAccountId,
        UUID toAccountId,
        Money amount,
        String reference
) {
    public SendMoneyCommand {
        if (fromAccountId == null) {
            throw new IllegalArgumentException("Sender account ID cannot be null");
        }
        if (toAccountId == null) {
            throw new IllegalArgumentException("Receiver account ID cannot be null");
        }
        if (amount == null || !amount.isPositive()) {
            throw new IllegalArgumentException("Amount must be positive and non-null");
        }
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("Transaction reference is required");
        }
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer money to the same account");
        }
    }
}