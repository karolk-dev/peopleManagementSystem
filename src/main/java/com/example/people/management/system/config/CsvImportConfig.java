package com.example.people.management.system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "import")
@Getter
@Setter
public class CsvImportConfig {
    private Integer batchSize;
    private String personTypeColumn;
}
