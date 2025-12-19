package com.distributed.ledger.infrastructure.adapter.persistence.mapper;

import com.distributed.ledger.domain.model.AccountId;
import com.distributed.ledger.domain.model.Money;
import com.distributed.ledger.domain.model.Transaction;
import com.distributed.ledger.domain.model.TransactionStatus;
import com.distributed.ledger.domain.model.TransactionType;
import com.distributed.ledger.infrastructure.adapter.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toDomain(TransactionEntity entity) {
        return Transaction.with(
                entity.getId(),
                entity.getReference(),
                entity.getFromAccountId() != null ? AccountId.of(entity.getFromAccountId()) : null,
                entity.getToAccountId() != null ? AccountId.of(entity.getToAccountId()) : null,
                Money.of(entity.getAmount(), entity.getCurrency()),
                TransactionType.valueOf(entity.getType()),
                TransactionStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getCompletedAt(),
                entity.getDescription()
        );
    }

    public TransactionEntity toEntity(Transaction domain) {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(domain.getId());
        entity.setReference(domain.getReference());
        entity.setFromAccountId(domain.getFromAccountId() != null ? domain.getFromAccountId().value() : null);
        entity.setToAccountId(domain.getToAccountId() != null ? domain.getToAccountId().value() : null);
        entity.setAmount(domain.getAmount().getAmount());
        entity.setCurrency(domain.getAmount().getCurrencyCode());
        entity.setType(domain.getType().name());
        entity.setStatus(domain.getStatus().name());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setCompletedAt(domain.getCompletedAt());
        entity.setDescription(domain.getDescription());
        return entity;
    }
}
