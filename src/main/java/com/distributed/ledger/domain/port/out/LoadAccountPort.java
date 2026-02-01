package com.distributed.ledger.domain.port.out;

import com.distributed.ledger.domain.model.Account;
import com.distributed.ledger.domain.model.AccountId;
import java.util.Optional;

public interface LoadAccountPort {
    Account loadAccount(AccountId accountId);
    Optional<Account> loadAccount(String accountNumber);
}