package com.distributed.ledger.domain.port.in;

public interface SendMoneyUseCase {
    boolean sendMoney(SendMoneyCommand command);
}