package com.distributed.ledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Main Spring Boot application class.
 * Located in infrastructure layer as it's framework-specific.
 *
 * Component scanning starts from this package and scans all sub-packages
 * as well as the application and domain layers.
 */
@EnableRetry
@SpringBootApplication(scanBasePackages = "com.distributed.ledger")
public class DistributedLedgerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributedLedgerApplication.class, args);
    }

}
