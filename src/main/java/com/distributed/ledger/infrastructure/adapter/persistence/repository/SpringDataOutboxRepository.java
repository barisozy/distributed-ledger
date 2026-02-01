package com.distributed.ledger.infrastructure.adapter.persistence.repository;

import com.distributed.ledger.infrastructure.adapter.persistence.entity.OutboxEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataOutboxRepository extends JpaRepository<OutboxEntity, UUID> {

    @Query(value = "SELECT * FROM outbox_events WHERE processed = false ORDER BY id ASC LIMIT :limit FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<OutboxEntity> findBatchToProcess(@Param("limit") int limit);

    @Modifying
    @Query("DELETE FROM OutboxEntity o WHERE o.processed = true AND o.updatedAt < :threshold")
    int deleteByProcessedTrueAndUpdatedAtBefore(@Param("threshold") LocalDateTime threshold);
}