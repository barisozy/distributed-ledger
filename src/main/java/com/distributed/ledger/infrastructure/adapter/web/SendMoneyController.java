package com.distributed.ledger.infrastructure.adapter.web;

import com.distributed.ledger.domain.model.Money;
import com.distributed.ledger.domain.port.in.SendMoneyCommand;
import com.distributed.ledger.domain.port.in.SendMoneyUseCase;
import com.distributed.ledger.infrastructure.adapter.web.dto.ApiErrorResponse;
import com.distributed.ledger.infrastructure.adapter.web.dto.SendMoneyRequest;
import com.distributed.ledger.infrastructure.adapter.web.mapper.SendMoneyMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "Money transfer and transaction operations")
public class SendMoneyController {

    private final SendMoneyUseCase sendMoneyUseCase;
    private final SendMoneyMapper sendMoneyMapper;

    @Operation(summary = "Send Money", description = "Transfers money from one account to another with idempotency check and optimistic locking.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction processed successfully (or idempotent response)",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., negative amount, missing fields)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Concurrency conflict or Idempotency violation",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Business rule violation (e.g., Insufficient funds)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/send")
    public ResponseEntity<Void> sendMoney(@RequestBody @Valid SendMoneyRequest request) {
        SendMoneyCommand command = sendMoneyMapper.toCommand(request);
        boolean success = sendMoneyUseCase.sendMoney(command);
        if (!success) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }
}