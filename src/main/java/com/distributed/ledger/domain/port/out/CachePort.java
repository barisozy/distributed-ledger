package com.distributed.ledger.domain.port.out;

import java.time.Duration;

public interface CachePort {
    void put(String key, String value, Duration ttl);
    boolean exists(String key);
}