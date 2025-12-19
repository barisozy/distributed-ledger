package com.distributed.ledger.infrastructure.adapter.web.dto;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        String errorCode,
        String message,
        LocalDateTime timestamp
) {
    public static ApiErrorResponse of(String errorCode, String message) {
        return new ApiErrorResponse(errorCode, message, LocalDateTime.now());
    }
}