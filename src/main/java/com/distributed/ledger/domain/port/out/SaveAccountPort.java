package com.distributed.ledger.domain.port.out;

import com.distributed.ledger.domain.model.Account;

public interface SaveAccountPort {
    void saveAccount(Account account);
}