package com.example.people.management.system.service.importJDBC;

import com.example.people.management.system.config.ImportRedissonConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncImportInitializer {

    private final RedissonClient redissonClient;
    private final ImportRedissonConfig importConfig;
    private final AsyncImportRunner runner;

    @PostConstruct
    public void initSemaphore() {
        RPermitExpirableSemaphore semaphore = redissonClient.getPermitExpirableSemaphore(importConfig.getKey());
        if (!semaphore.isExists()) {
            semaphore.trySetPermits(importConfig.getConcurrentLimit());
        } else {
            semaphore.delete();
            semaphore.trySetPermits(importConfig.getConcurrentLimit());
        }
        runner.startConsumer();
    }
}
