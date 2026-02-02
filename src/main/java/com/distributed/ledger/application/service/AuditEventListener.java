package com.distributed.ledger.application.service;

import com.distributed.ledger.domain.event.TransactionCreatedEvent;
import com.distributed.ledger.domain.port.out.SaveAuditLogPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditEventListener {

    private final SaveAuditLogPort saveAuditLogPort;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        try {
            UUID entityId = UUID.fromString(event.getEventId());

            // String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
            String currentUserId = "SYSTEM_TEMP";

            Map<String, Object> changes = new HashMap<>();
            changes.put("amount", event.getAmount());
            changes.put("currency", event.getCurrency());
            changes.put("fromAccount", event.getFromAccountId());
            changes.put("toAccount", event.getToAccountId());

            saveAuditLogPort.save(
                    "TRANSACTION",
                    entityId,
                    "CREATED",
                    currentUserId,
                    changes,
                    null,
                    "Distributed-Ledger-Service"
            );

            log.info("Audit log saved successfully for Transaction: {}", entityId);

        } catch (Exception e) {
            log.error("FATAL: Failed to save audit log. Transaction will be ROLLED BACK. Event: {}", event.getEventId(), e);
            throw new RuntimeException("Audit log failure caused transaction rollback", e);
        }
    }
}