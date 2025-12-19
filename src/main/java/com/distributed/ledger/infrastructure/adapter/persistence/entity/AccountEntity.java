package com.distributed.ledger.infrastructure.adapter.persistence.entity;

import com.distributed.ledger.infrastructure.adapter.persistence.converter.PiiCryptoConverter; // Import ekle
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
public class AccountEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    // PII DATA - ŞİFRELENDİ
    @Convert(converter = PiiCryptoConverter.class)
    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    // PII DATA - ŞİFRELENDİ
    @Convert(converter = PiiCryptoConverter.class)
    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "status", nullable = false)
    private String status;

    @Version
    @Column(name = "version")
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}