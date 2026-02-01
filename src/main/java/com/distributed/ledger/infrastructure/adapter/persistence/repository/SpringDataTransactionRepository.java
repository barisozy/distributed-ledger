package com.distributed.ledger.infrastructure.adapter.persistence.repository;

import com.distributed.ledger.infrastructure.adapter.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataTransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    boolean existsByReference(String reference);
}