package com.distributed.ledger.infrastructure.adapter.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record SendMoneyRequest(
        @NotNull(message = "Sender account ID is required")
        UUID fromAccountId,

        @NotNull(message = "Receiver account ID is required")
        UUID toAccountId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Currency code is required")
        String currency,

        @NotBlank(message = "Reference is required for idempotency")
        String reference
) {}