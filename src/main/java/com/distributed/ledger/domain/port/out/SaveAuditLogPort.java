package com.distributed.ledger.domain.port.out;

import java.util.Map;
import java.util.UUID;

public interface SaveAuditLogPort {
    void save(String entityType, UUID entityId, String action, String userId, Map<String, Object> changes, String ipAddress, String userAgent);
}