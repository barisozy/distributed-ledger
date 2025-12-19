package com.distributed.ledger.integration;

import com.distributed.ledger.DistributedLedgerApplication;
import com.distributed.ledger.infrastructure.adapter.persistence.entity.AccountEntity;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataAccountRepository;
import com.distributed.ledger.infrastructure.adapter.web.dto.SendMoneyRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DistributedLedgerApplication.class) // Garanti olsun diye sınıfı belirttik
@AutoConfigureMockMvc
public class MoneyTransferIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataAccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private AccountEntity sender;
    private AccountEntity receiver;

    @BeforeEach
    void setup() {
        // Her testten önce temizlik ve veri hazırlığı
        accountRepository.deleteAll();

        // 1. Gönderici: 1000 TL
        sender = new AccountEntity();
        sender.setId(UUID.randomUUID());
        sender.setAccountNumber("TR01");
        sender.setAccountName("Alice");
        sender.setBalance(new BigDecimal("1000.00"));
        sender.setCurrency("TRY");
        sender.setStatus("ACTIVE");
        sender.setVersion(0L);
        accountRepository.save(sender);

        // 2. Alıcı: 0 TL
        receiver = new AccountEntity();
        receiver.setId(UUID.randomUUID());
        receiver.setAccountNumber("TR02");
        receiver.setAccountName("Bob");
        receiver.setBalance(new BigDecimal("0.00"));
        receiver.setCurrency("TRY");
        receiver.setStatus("ACTIVE");
        receiver.setVersion(0L);
        accountRepository.save(receiver);
    }

    @Test
    @DisplayName("Happy Path: Başarılı para transferi bakiyeleri güncellemeli")
    void shouldTransferMoneySuccessfully() throws Exception {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        SendMoneyRequest request = new SendMoneyRequest(
                sender.getId(),
                receiver.getId(),
                amount,
                "TRY",
                "REF-" + UUID.randomUUID()
        );

        // When
        mockMvc.perform(post("/api/v1/transactions/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        AccountEntity updatedSender = accountRepository.findById(sender.getId()).orElseThrow();
        AccountEntity updatedReceiver = accountRepository.findById(receiver.getId()).orElseThrow();

        assertThat(updatedSender.getBalance()).isEqualByComparingTo("900.00");
        assertThat(updatedReceiver.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Concurrency: Retry mekanizması sayesinde tüm işlemler sonunda başarılı olmalı")
    void shouldHandleConcurrencyWithRetry() throws InterruptedException {
        // Senaryo: 5 Thread aynı anda Alice'in hesabından para çekmeye çalışıyor.
        // Beklenti: Optimistic Lock hataları çıksa bile @Retryable sayesinde sistem
        // işlemi tekrar dener ve hepsi (5'i de) günün sonunda 200 OK alır.

        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    // Her istek için benzersiz referans
                    SendMoneyRequest request = new SendMoneyRequest(
                            sender.getId(),
                            receiver.getId(),
                            new BigDecimal("100.00"),
                            "TRY",
                            "REF-" + UUID.randomUUID()
                    );

                    // Retry sayesinde 409 Conflict yerine 200 OK bekliyoruz
                    mockMvc.perform(post("/api/v1/transactions/send")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isOk());

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // Tüm threadlerin bitmesini bekle

        // Assertions (Kanıtlar)
        System.out.println("Başarılı İşlemler: " + successCount.get());
        System.out.println("Başarısız İşlemler: " + failureCount.get());

        // 1. Retry çalıştığı için tüm işlemler başarılı olmalı
        assertThat(successCount.get()).isEqualTo(5);
        assertThat(failureCount.get()).isEqualTo(0);

        // 2. Veritabanı tutarlılığı: 1000 - (5 * 100) = 500 kalmalı
        // Eğer Lock çalışmasaydı bakiye yanlış çıkardı.
        AccountEntity finalSender = accountRepository.findById(sender.getId()).orElseThrow();
        BigDecimal expectedBalance = new BigDecimal("1000.00")
                .subtract(new BigDecimal("100.00").multiply(new BigDecimal(numberOfThreads)));

        assertThat(finalSender.getBalance()).isEqualByComparingTo(expectedBalance);
    }
}