package com.distributed.ledger.infrastructure.adapter.persistence.mapper;

import com.distributed.ledger.domain.model.Account;
import com.distributed.ledger.domain.model.AccountId;
import com.distributed.ledger.domain.model.AccountStatus;
import com.distributed.ledger.domain.model.Money;
import com.distributed.ledger.infrastructure.adapter.persistence.entity.AccountEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public Account toDomain(AccountEntity entity) {
        return Account.with(
                AccountId.of(entity.getId()),
                entity.getAccountName(),
                entity.getAccountNumber(),
                Money.of(entity.getBalance(), entity.getCurrency()),
                AccountStatus.valueOf(entity.getStatus().name()),
                entity.getVersion()
        );
    }

    public AccountEntity toEntity(Account domain) {
        AccountEntity entity = new AccountEntity();
        entity.setId(domain.getId().value());
        entity.setAccountNumber(domain.getAccountNumber().value());
        entity.setAccountName(domain.getName());
        entity.setBalance(domain.getBalance().getAmount());
        entity.setCurrency(domain.getBalance().getCurrencyCode());
        entity.setStatus(domain.getStatus());
        if (domain.getVersion() != null) {
            entity.setVersion(domain.getVersion());
        }
        return entity;
    }

    public void updateEntity(Account domain, AccountEntity entity) {
        entity.setAccountNumber(domain.getAccountNumber().value());
        entity.setAccountName(domain.getName());
        entity.setBalance(domain.getBalance().getAmount());
        entity.setCurrency(domain.getBalance().getCurrencyCode());
        entity.setStatus(domain.getStatus());
        entity.setVersion(domain.getVersion());
    }
}