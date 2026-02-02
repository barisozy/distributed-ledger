package com.distributed.ledger.infrastructure.adapter.persistence;

import com.distributed.ledger.domain.model.Account;
import com.distributed.ledger.domain.model.AccountId;
import com.distributed.ledger.domain.port.out.LoadAccountPort;
import com.distributed.ledger.domain.port.out.SaveAccountPort;
import com.distributed.ledger.infrastructure.adapter.persistence.entity.AccountEntity;
import com.distributed.ledger.infrastructure.adapter.persistence.mapper.AccountMapper;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Component
public class AccountPersistenceAdapter implements LoadAccountPort, SaveAccountPort {

    private final SpringDataAccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final String pepper;

    public AccountPersistenceAdapter(SpringDataAccountRepository accountRepository,
                                     AccountMapper accountMapper,
                                     @Value("${security.hash.pepper}") String pepper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.pepper = pepper;
    }

    @Override
    public Account loadAccount(AccountId accountId) {
        AccountEntity entity = accountRepository.findById(accountId.value())
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + accountId.value()));

        return accountMapper.toDomain(entity);
    }

    @Override
    public Optional<Account> loadAccount(String accountNumber) {
        String hash = computeHash(accountNumber);
        return accountRepository.findByAccountNumberHash(hash)
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

        String hash = computeHash(account.getAccountNumber().value());
        entity.setAccountNumberHash(hash);

        accountRepository.saveAndFlush(entity);
    }

    private String computeHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String inputWithPepper = input + pepper;
            byte[] encodedHash = digest.digest(inputWithPepper.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }
}