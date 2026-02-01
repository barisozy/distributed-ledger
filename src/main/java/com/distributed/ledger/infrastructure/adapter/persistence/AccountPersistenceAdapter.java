package com.distributed.ledger.infrastructure.adapter.persistence;

import com.distributed.ledger.domain.exception.DomainException;
import com.distributed.ledger.domain.model.Account;
import com.distributed.ledger.domain.model.AccountId;
import com.distributed.ledger.domain.port.out.LoadAccountPort;
import com.distributed.ledger.domain.port.out.SaveAccountPort;
import com.distributed.ledger.infrastructure.adapter.persistence.entity.AccountEntity;
import com.distributed.ledger.infrastructure.adapter.persistence.mapper.AccountMapper;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountPersistenceAdapter implements LoadAccountPort, SaveAccountPort {

    private final SpringDataAccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    public Account loadAccount(AccountId accountId) {
        AccountEntity entity = accountRepository.findById(accountId.value())
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + accountId.value()));

        return accountMapper.toDomain(entity);
    }

    @Override
    public Optional<Account> loadAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(accountMapper::toDomain);
    }

    @Override
    public void saveAccount(Account account) {
        AccountEntity entity = accountRepository.findById(account.getId().value())
                .map(existingEntity -> {
                    accountMapper.updateEntity(account, existingEntity);
                    return existingEntity;
                })
                .orElseGet(() -> accountMapper.toEntity(account));

        accountRepository.saveAndFlush(entity);
    }
}