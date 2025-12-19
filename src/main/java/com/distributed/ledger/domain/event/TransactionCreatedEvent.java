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

    // Constructor: Domain Entity -> Event
    public TransactionCreatedEvent(Transaction transaction) {
        // 1. Interface gereği unique ID
        this.eventId = UUID.randomUUID().toString();

        // 2. Domain verilerini al
        this.transactionId = TransactionId.of(transaction.getId());
        this.fromAccountId = transaction.getFromAccountId();
        this.toAccountId = transaction.getToAccountId();

        // 3. Hassas para birimi dönüşümü (Scientific notation riskini önle)
        this.amount = transaction.getAmount().getAmount().toPlainString();
        this.currency = transaction.getAmount().getCurrency().getCurrencyCode();

        // 4. Interface gereği Instant (Şu an)
        this.occurredOn = Instant.now();
    }

    // Lombok @Getter zaten getEventId, getOccurredOn metodlarını oluşturur
    // ama interface override'ını açıkça belirtmek ve getEventType'ı eklemek için:

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
        // Kafka topic routing veya log filtreleme için sabit tip
        return "TRANSACTION_CREATED";
    }
}