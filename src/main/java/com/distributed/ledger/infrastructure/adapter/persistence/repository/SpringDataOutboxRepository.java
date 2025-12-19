package com.distributed.ledger.infrastructure.adapter.persistence.repository;

import com.distributed.ledger.infrastructure.adapter.persistence.entity.OutboxEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataOutboxRepository extends JpaRepository<OutboxEntity, UUID> {

    // Scheduler'ın işlenmemiş kayıtları çekmesi için.
    // Pageable parametresi ile "bana ilk 50 taneyi ver" diyebileceğiz.
    List<OutboxEntity> findByProcessedFalseOrderByIdAsc(Pageable pageable);
}