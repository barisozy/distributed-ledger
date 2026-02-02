package com.distributed.ledger.infrastructure.adapter.persistence.mapper;

import com.distributed.ledger.domain.model.LedgerEntry;
import com.distributed.ledger.infrastructure.adapter.persistence.entity.LedgerEntryEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class LedgerEntryMapper {

    public LedgerEntryEntity toEntity(LedgerEntry domain) {
        LedgerEntryEntity entity = new LedgerEntryEntity();
        entity.setId(domain.id());
        entity.setTransactionId(domain.transactionId().value());
        entity.setAccountId(domain.accountId().value());
        entity.setType(domain.type().name());
        entity.setAmount(domain.amount().getAmount());
        entity.setCurrency(domain.amount().getCurrencyCode());
        entity.setBalanceAfter(domain.balanceAfter().getAmount());
        entity.setCreatedAt(LocalDateTime.ofInstant(domain.createdAt(), ZoneId.of("UTC")));
        return entity;
    }
}