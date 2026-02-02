package com.distributed.ledger.application.service;

import com.distributed.ledger.domain.model.Money;
import com.distributed.ledger.domain.port.in.SendMoneyCommand;
import com.distributed.ledger.domain.port.out.CachePort;
import com.distributed.ledger.domain.port.out.DistributedLockPort;
import com.distributed.ledger.domain.port.out.SaveTransactionPort;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendMoneyServiceTest {

    @Mock private TransferExecutor transferExecutor;
    @Mock private SaveTransactionPort saveTransactionPort;
    @Mock private DistributedLockPort distributedLockPort;
    @Mock private CachePort cachePort;

    private SendMoneyService sendMoneyService;

    @BeforeEach
    void setUp() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        sendMoneyService = new SendMoneyService(
                transferExecutor,
                saveTransactionPort,
                distributedLockPort,
                cachePort,
                meterRegistry
        );
    }

    @Test
    @DisplayName("Should execute transfer successfully when lock is acquired")
    void shouldExecuteTransferSuccessfully() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        String ref = "TX-123";
        SendMoneyCommand command = new SendMoneyCommand(fromId, toId, Money.of(BigDecimal.TEN, "USD"), ref);

        when(cachePort.exists(anyString())).thenReturn(false);
        when(saveTransactionPort.existsByReference(ref)).thenReturn(false);

        doAnswer(invocation -> {
            Runnable action = invocation.getArgument(1);
            action.run();
            return null;
        }).when(distributedLockPort).executeInLock(anyString(), any(Runnable.class));

        boolean result = sendMoneyService.sendMoney(command);

        assertThat(result).isTrue();

        verify(transferExecutor).execute(command);

        verify(cachePort).put(contains(ref), eq("COMPLETED"), any());
    }

    @Test
    @DisplayName("Should skip execution if transaction is already in cache (Idempotency)")
    void shouldSkipIfInCache() {
        String ref = "TX-EXISTING";
        SendMoneyCommand command = new SendMoneyCommand(UUID.randomUUID(), UUID.randomUUID(), Money.of(BigDecimal.TEN, "USD"), ref);

        when(cachePort.exists(contains(ref))).thenReturn(true);

        boolean result = sendMoneyService.sendMoney(command);

        assertThat(result).isTrue();
        verify(transferExecutor, never()).execute(any());
        verify(distributedLockPort, never()).executeInLock(anyString(), any());
    }
}