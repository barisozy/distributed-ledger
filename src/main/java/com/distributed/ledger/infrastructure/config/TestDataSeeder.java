package com.distributed.ledger.infrastructure.config;

import com.distributed.ledger.domain.model.AccountStatus;
import com.distributed.ledger.infrastructure.adapter.persistence.entity.AccountEntity;
import com.distributed.ledger.infrastructure.adapter.persistence.repository.SpringDataAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TestDataSeeder {

    private final SpringDataAccountRepository accountRepository;

    @Bean
    @Profile("dev")
    public CommandLineRunner initData() {
        return args -> {
            if (accountRepository.count() > 0) {
                log.info("Database already contains data, skipping seed operation.");
                return;
            }

            log.info("Loading test data (encrypted)...");

            AccountEntity alice = new AccountEntity();
            alice.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
            alice.setAccountNumber("TR01");
            alice.setAccountNumberHash(hash("TR01"));
            alice.setAccountName("Alice");
            alice.setBalance(new BigDecimal("1000.00"));
            alice.setCurrency("TRY");
            alice.setStatus(AccountStatus.ACTIVE);
            alice.setCreatedAt(LocalDateTime.now());
            alice.setUpdatedAt(LocalDateTime.now());

            AccountEntity bob = new AccountEntity();
            bob.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
            bob.setAccountNumber("TR02");
            bob.setAccountNumberHash(hash("TR02"));
            bob.setAccountName("Bob");
            bob.setBalance(BigDecimal.ZERO);
            bob.setCurrency("TRY");
            bob.setStatus(AccountStatus.ACTIVE);
            bob.setCreatedAt(LocalDateTime.now());
            bob.setUpdatedAt(LocalDateTime.now());

            accountRepository.save(alice);
            accountRepository.save(bob);

            log.info("Test data loaded successfully!");
        };
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}