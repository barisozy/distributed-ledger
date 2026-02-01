package com.distributed.ledger.infrastructure.adapter.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        log.info("Health check called on thread: {} (Is Virtual: {})",
                Thread.currentThread(),
                Thread.currentThread().isVirtual());

        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "distributed-ledger");
        return response;
    }
}


