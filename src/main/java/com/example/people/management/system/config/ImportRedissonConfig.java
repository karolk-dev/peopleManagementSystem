package com.example.people.management.system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "importer")
@Getter
@Setter
public class ImportRedissonConfig {
    private Long leaseTime;
    private String key;
    private Integer concurrentLimit;
    private Integer waitTimeoutSeconds;
    private String queueKey;
}
