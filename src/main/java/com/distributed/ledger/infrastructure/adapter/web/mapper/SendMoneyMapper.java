package com.distributed.ledger.infrastructure.adapter.web.mapper;

import com.distributed.ledger.domain.model.Money;
import com.distributed.ledger.domain.port.in.SendMoneyCommand;
import com.distributed.ledger.infrastructure.adapter.web.dto.SendMoneyRequest;
import org.springframework.stereotype.Component;

@Component
public class SendMoneyMapper {

    public SendMoneyCommand toCommand(SendMoneyRequest request) {
        return new SendMoneyCommand(
                request.fromAccountId(),
                request.toAccountId(),
                Money.of(request.amount(), request.currency()),
                request.reference()
        );
    }
}