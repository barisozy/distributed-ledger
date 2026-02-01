package com.distributed.ledger.infrastructure.adapter.persistence.repository;

import com.distributed.ledger.infrastructure.adapter.persistence.entity.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataLedgerEntryRepository extends JpaRepository<LedgerEntryEntity, UUID> {
}
