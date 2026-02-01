package com.distributed.ledger.infrastructure.adapter.persistence;

import com.distributed.ledger.domain.model.Transaction;
import com.distributed.ledger.domain.port.out.SaveTransactionPort;
import com.distributed.ledger.infrastructure.adapter.persistence.entity.TransactionEntity;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements SaveTransactionPort {

    private final SpringDataTransactionRepository transactionRepository;

    @Override
    public void saveTransaction(Transaction transaction) {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(transaction.getId());
        entity.setReference(transaction.getReference());
        entity.setFromAccountId(transaction.getFromAccountId().value());
        entity.setToAccountId(transaction.getToAccountId().value());
        entity.setAmount(transaction.getAmount().getAmount());
        entity.setCurrency(transaction.getAmount().getCurrencyCode());
        entity.setType(transaction.getAmount().getAmount().signum() > 0 ? "TRANSFER" : "UNKNOWN");
        entity.setStatus(transaction.getStatus().name());
        entity.setCreatedAt(transaction.getCreatedAt());

        transactionRepository.save(entity);
    }

    @Override
    public boolean existsByReference(String reference) {
        return transactionRepository.existsByReference(reference);
    }
}