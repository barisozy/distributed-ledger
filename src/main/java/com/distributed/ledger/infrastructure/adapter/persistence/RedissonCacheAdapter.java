package com.distributed.ledger.infrastructure.adapter.persistence;

import com.distributed.ledger.domain.port.out.CachePort;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedissonCacheAdapter implements CachePort {

    private final RedissonClient redissonClient;

    @Override
    public void put(String key, String value, Duration ttl) {
        redissonClient.getBucket(key).set(value, ttl);
    }

    @Override
    public boolean exists(String key) {
        return redissonClient.getBucket(key).isExists();
    }
}