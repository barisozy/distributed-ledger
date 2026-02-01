package com.distributed.ledger.application.service;

import com.distributed.ledger.domain.port.in.SendMoneyCommand;
import com.distributed.ledger.domain.port.out.SaveTransactionPort;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendMoneyServiceTest {

    @Mock TransferExecutor transferExecutor;
    @Mock SaveTransactionPort saveTransactionPort;
    @Mock RedissonClient redissonClient;
    @Mock RLock rLock;
    @Mock RBucket<Object> rBucket;

    private SendMoneyService sendMoneyService;

    @BeforeEach
    void setUp() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        sendMoneyService = new SendMoneyService(transferExecutor, saveTransactionPort, redissonClient, meterRegistry);
    }

    @Test
    void shouldExecuteTransferSuccessfully() throws InterruptedException {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        String ref = "TX-123";
        SendMoneyCommand command = new SendMoneyCommand(fromId, toId, com.distributed.ledger.domain.model.Money.of(BigDecimal.TEN, "USD"), ref);

        when(redissonClient.getBucket(anyString())).thenReturn(rBucket);
        when(rBucket.isExists()).thenReturn(false);
        when(saveTransactionPort.existsByReference(ref)).thenReturn(false);
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        boolean result = sendMoneyService.sendMoney(command);

        assertThat(result).isTrue();
        verify(transferExecutor).execute(command);
    }
}