package com.distributed.ledger.infrastructure.adapter.persistence;

import com.distributed.ledger.domain.port.out.SaveAuditLogPort;
import com.distributed.ledger.infrastructure.adapter.persistence.entity.AuditLogEntity;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditPersistenceAdapter implements SaveAuditLogPort {

    private final SpringDataAuditLogRepository repository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void save(String entityType, UUID entityId, String action, String userId, Map<String, Object> changes, String ipAddress, String userAgent) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setId(UUID.randomUUID());
        entity.setEntityType(entityType);
        entity.setEntityId(entityId);
        entity.setAction(action);
        entity.setUserId(userId);
        entity.setChanges(changes);
        entity.setIpAddress(ipAddress);
        entity.setUserAgent(userAgent);
        entity.setCreatedAt(LocalDateTime.now());

        repository.save(entity);
    }
}