package com.distributed.ledger.domain.port.out;

import com.distributed.ledger.domain.model.Transaction;

public interface SaveTransactionPort {
    void saveTransaction(Transaction transaction);
    boolean existsByReference(String reference);
}