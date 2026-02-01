package com.distributed.ledger.infrastructure.adapter.persistence;

import com.distributed.ledger.domain.model.LedgerEntry;
import com.distributed.ledger.domain.port.out.SaveLedgerEntryPort;
import com.distributed.ledger.infrastructure.adapter.persistence.entity.LedgerEntryEntity;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataLedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LedgerPersistenceAdapter implements SaveLedgerEntryPort {

    private final SpringDataLedgerEntryRepository repository;

    @Override
    public void saveAll(List<LedgerEntry> entries) {
        List<LedgerEntryEntity> entities = entries.stream()
                .map(this::toEntity)
                .toList();
        repository.saveAll(entities);
    }

    private LedgerEntryEntity toEntity(LedgerEntry domain) {
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