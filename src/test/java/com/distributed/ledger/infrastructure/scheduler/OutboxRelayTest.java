package com.distributed.ledger.infrastructure.scheduler;

import com.distributed.ledger.infrastructure.adapter.persistence.entity.OutboxEntity;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataOutboxRepository;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock private SpringDataOutboxRepository outboxRepository;
    @Mock private KafkaTemplate<String, String> kafkaTemplate;
    @Mock private MeterRegistry meterRegistry;
    @Mock private MeterRegistry.Config registryConfig;
    @Mock private Clock clock;
    @Mock private Timer timer;
    @Mock private Counter counter;

    @InjectMocks
    private OutboxRelay outboxRelay;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(outboxRelay, "batchSize", 50);
        ReflectionTestUtils.setField(outboxRelay, "kafkaTopic", "test-topic");
        ReflectionTestUtils.setField(outboxRelay, "maxRetries", 3);
        ReflectionTestUtils.setField(outboxRelay, "retentionDays", 7);
        ReflectionTestUtils.setField(outboxRelay, "publishTimeoutSec", 2);

        lenient().when(meterRegistry.config()).thenReturn(registryConfig);
        lenient().when(registryConfig.clock()).thenReturn(clock);

        lenient().when(meterRegistry.timer(anyString())).thenReturn(timer);
        lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
        lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
    }

    @Test
    void shouldDoNothingWhenNoEventsFound() {
        given(outboxRepository.findBatchToProcess(50)).willReturn(Collections.emptyList());

        outboxRelay.processOutbox();

        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void shouldProcessEventSuccessfully() {
        OutboxEntity event = OutboxEntity.builder()
                .id(UUID.randomUUID())
                .aggregateId("AGG-1")
                .payload("{}")
                .processed(false)
                .build();

        given(outboxRepository.findBatchToProcess(50)).willReturn(List.of(event));

        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        given(kafkaTemplate.send(anyString(), anyString(), anyString())).willReturn(future);

        outboxRelay.processOutbox();

        verify(kafkaTemplate).send("test-topic", "AGG-1", "{}");
        verify(outboxRepository).save(argThat(savedEvent -> savedEvent.isProcessed() && savedEvent.getErrorMessage() == null));
    }

    @Test
    void shouldHandleFailureAndIncrementRetry() {
        OutboxEntity event = OutboxEntity.builder()
                .id(UUID.randomUUID())
                .aggregateId("AGG-FAIL")
                .payload("{}")
                .retryCount(0)
                .build();

        given(outboxRepository.findBatchToProcess(50)).willReturn(List.of(event));

        CompletableFuture<SendResult<String, String>> failedFuture = CompletableFuture.failedFuture(new RuntimeException("Kafka Down"));
        given(kafkaTemplate.send(anyString(), anyString(), anyString())).willReturn(failedFuture);

        outboxRelay.processOutbox();

        verify(outboxRepository).save(argThat(savedEvent ->
                !savedEvent.isProcessed() &&
                        savedEvent.getRetryCount() == 1 &&
                        savedEvent.getErrorMessage().contains("Kafka Down")
        ));
    }

    @Test
    void shouldMarkAsPoisonPillWhenMaxRetriesReached() {
        OutboxEntity event = OutboxEntity.builder()
                .id(UUID.randomUUID())
                .retryCount(2)
                .build();

        given(outboxRepository.findBatchToProcess(50)).willReturn(List.of(event));

        CompletableFuture<SendResult<String, String>> failedFuture = CompletableFuture.failedFuture(new RuntimeException("Fatal Error"));
        given(kafkaTemplate.send(anyString(), anyString(), anyString())).willReturn(failedFuture);

        outboxRelay.processOutbox();

        verify(outboxRepository).save(argThat(savedEvent ->
                savedEvent.isProcessed() &&
                        savedEvent.getRetryCount() == 3
        ));
    }

    @Test
    void shouldCleanupOldEvents() {
        given(outboxRepository.deleteByProcessedTrueAndUpdatedAtBefore(any(LocalDateTime.class))).willReturn(10);

        outboxRelay.cleanupProcessedEvents();

        verify(outboxRepository).deleteByProcessedTrueAndUpdatedAtBefore(any(LocalDateTime.class));
        verify(counter).increment(10);
    }
}