package com.distributed.ledger.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Tüm entegrasyon testleri için temel sınıf.
 * Testcontainers kullanarak gerçek bir ortam (DB, Cache, Broker) simüle eder.
 * "Singleton Container" pattern kullanılarak test performansı optimize edilmiştir.
 */
public abstract class IntegrationTestBase {

    // 1. PostgreSQL Konteyneri (Veritabanı)
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("distributed_ledger_test")
            .withUsername("testuser")
            .withPassword("testpass");

    // 2. Redis Konteyneri (Cache & Distributed Lock)
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    // 3. Kafka Konteyneri (Messaging)
    // Confluent'in Kafka imajını kullanıyoruz (Kraft modu yerine standart Zookeeper'lı veya Kraft'lı versiyon fark etmez, KafkaContainer bunu yönetir)
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    // Static blok ile konteynerleri test suite başında bir kere başlatıyoruz.
    static {
        POSTGRES.start();
        REDIS.start();
        KAFKA.start();
    }

    // Konteynerlerin rastgele atadığı portları Spring Context'e enjekte ediyoruz.
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL Ayarları
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        // Flyway Ayarları (DB migration'ın testte de çalışması için)
        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);

        // Redis Ayarları
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));

        // Kafka Ayarları
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }
}