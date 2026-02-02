package com.distributed.ledger.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestBase {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ledger_db")
            .withUsername("ledger_user")
            .withPassword("secret-ledger-password");

    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.2"))
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", "test-redis-pass");

    static {
        postgres.start();
        kafka.start();
        redis.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "test-redis-pass");
        registry.add("spring.redis.password", () -> "test-redis-pass");

        String secureKey = "12345678901234567890123456789012";
        registry.add("ENCRYPTION_KEY", () -> secureKey);
        registry.add("security.encryption.key", () -> secureKey);
        registry.add("security.hash.pepper", () -> "test-environment-dummy-pepper-value-123456");

        registry.add("ADMIN_USER", () -> "test-admin");
        registry.add("security.user.name", () -> "test-admin");

        registry.add("ADMIN_PASSWORD", () -> "test-admin-pass");
        registry.add("security.user.password", () -> "test-admin-pass");
    }
}