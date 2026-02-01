package com.distributed.ledger.domain.port.out;

import com.distributed.ledger.domain.model.LedgerEntry;
import java.util.List;

public interface SaveLedgerEntryPort {
    void saveAll(List<LedgerEntry> entries);
}