package com.example.people.management.system;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
public class TestContainersConfig {
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    public static final GenericContainer REDIS = new FixedHostPortGenericContainer("redis:latest")
            .withFixedExposedPort(6379, 6379);

    @Container
    public static final MinIOContainer MINIO =
            new MinIOContainer("minio/minio:latest");


    static {
        POSTGRES.start();
        REDIS.start();
        MINIO.start();

        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(MINIO.getS3URL().toString())
                    .credentials(MINIO.getUserName(), MINIO.getPassword())
                    .build();

            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket("test-bucket").build()
            );
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("test-bucket").build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MinIO bucket", e);
        }
    }


    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);
        registry.add("minio.endpoint", () -> MINIO.getS3URL().toString());
        registry.add("minio.accessKey", MINIO::getUserName);
        registry.add("minio.secretKey", MINIO::getPassword);
        registry.add("minio.bucket", () -> "test-bucket");
    }
}
