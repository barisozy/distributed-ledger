package com.distributed.ledger.infrastructure.config;

import com.distributed.ledger.infrastructure.adapter.persistence.entity.AccountEntity;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TestDataSeeder {

    private final SpringDataAccountRepository accountRepository;

    // Sadece 'dev' profilinde çalışsın, prod verisini bozmasın!
    @Bean
    @Profile("dev")
    public CommandLineRunner initData() {
        return args -> {
            if (accountRepository.count() > 0) {
                log.info("Veritabanında veri var, seed işlemi atlanıyor.");
                return;
            }

            log.info("Test verileri yükleniyor (Şifrelenmiş olarak)...");

            // 1. Gönderici (Alice)
            AccountEntity alice = new AccountEntity();
            alice.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
            alice.setAccountNumber("TR01");
            alice.setAccountName("Alice"); // Converter bunu otomatik şifreleyecek
            alice.setBalance(new BigDecimal("1000.00"));
            alice.setCurrency("TRY");
            alice.setStatus("ACTIVE");

            // 2. Alıcı (Bob)
            AccountEntity bob = new AccountEntity();
            bob.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
            bob.setAccountNumber("TR02");
            bob.setAccountName("Bob"); // Converter bunu otomatik şifreleyecek
            bob.setBalance(BigDecimal.ZERO);
            bob.setCurrency("TRY");
            bob.setStatus("ACTIVE");

            accountRepository.save(alice);
            accountRepository.save(bob);

            log.info("Test verileri başarıyla yüklendi!");
        };
    }
}