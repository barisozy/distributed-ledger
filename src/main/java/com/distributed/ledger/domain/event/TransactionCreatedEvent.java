package com.distributed.ledger.domain.event;

import com.distributed.ledger.domain.model.AccountId;
import com.distributed.ledger.domain.model.Transaction;
import com.distributed.ledger.domain.model.TransactionId;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Getter
@ToString
public class TransactionCreatedEvent implements DomainEvent {

    private final String eventId;
    private final TransactionId transactionId;
    private final AccountId fromAccountId;
    private final AccountId toAccountId;
    private final String amount;
    private final String currency;
    private final Instant occurredOn;

    public TransactionCreatedEvent(Transaction transaction) {
        this.eventId = UUID.randomUUID().toString();

        this.transactionId = TransactionId.of(transaction.getId());
        this.fromAccountId = transaction.getFromAccountId();
        this.toAccountId = transaction.getToAccountId();

        this.amount = transaction.getAmount().getAmount().toPlainString();
        this.currency = transaction.getAmount().getCurrency().getCurrencyCode();

        this.occurredOn = Instant.now();
    }

    @Override
    public String getEventId() {
        return this.eventId;
    }

    @Override
    public Instant getOccurredOn() {
        return this.occurredOn;
    }

    @Override
    public String getEventType() {
        return "TRANSACTION_CREATED";
    }
}