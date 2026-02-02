package com.distributed.ledger.infrastructure.adapter.persistence;

import com.distributed.ledger.domain.model.LedgerEntry;
import com.distributed.ledger.domain.port.out.SaveLedgerEntryPort;
import com.distributed.ledger.infrastructure.adapter.persistence.entity.LedgerEntryEntity;
import com.distributed.ledger.infrastructure.adapter.persistence.mapper.LedgerEntryMapper;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataLedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LedgerPersistenceAdapter implements SaveLedgerEntryPort {

    private final SpringDataLedgerEntryRepository repository;
    private final LedgerEntryMapper ledgerEntryMapper;

    @Override
    public void saveAll(List<LedgerEntry> entries) {
        List<LedgerEntryEntity> entities = entries.stream()
                .map(ledgerEntryMapper::toEntity)
                .toList();
        repository.saveAll(entities);
    }
}