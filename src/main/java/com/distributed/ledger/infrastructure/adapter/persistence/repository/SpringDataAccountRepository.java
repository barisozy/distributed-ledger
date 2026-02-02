package com.distributed.ledger.infrastructure.adapter.persistence.repository;

import com.distributed.ledger.infrastructure.adapter.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataAccountRepository extends JpaRepository<AccountEntity, UUID> {
    Optional<AccountEntity> findByAccountNumberHash(String accountNumberHash);
}