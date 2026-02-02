package com.distributed.ledger.infrastructure.adapter.persistence;

import com.distributed.ledger.domain.model.Transaction;
import com.distributed.ledger.domain.port.out.SaveTransactionPort;
import com.distributed.ledger.infrastructure.adapter.persistence.entity.TransactionEntity;
import com.distributed.ledger.infrastructure.adapter.persistence.mapper.TransactionMapper;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements SaveTransactionPort {

    private final SpringDataTransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public void saveTransaction(Transaction transaction) {
        TransactionEntity entity = transactionMapper.toEntity(transaction);
        transactionRepository.save(entity);
    }

    @Override
    public boolean existsByReference(String reference) {
        return transactionRepository.existsByReference(reference);
    }
}