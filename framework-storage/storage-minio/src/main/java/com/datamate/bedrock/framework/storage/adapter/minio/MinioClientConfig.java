package com.datamate.bedrock.framework.storage.adapter.minio;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import com.datamate.bedrock.framework.storage.domain.exception.StorageException;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO Client Configuration.
 * Configures the MinioClient instance with credentials and endpoint.
 * Employs Bedrock Logger and StorageException.
 */
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@RequiredArgsConstructor
public class MinioClientConfig {

    @EnableLogger
    private Logger log;

    private final MinioProperties properties;

    @Bean
    public MinioClient minioClient() {
        if (log != null) {
            log.info("Creating MinIO client at endpoint [{}]", properties.getEndpoint());
        }
        try {
            return MinioClient.builder()
                    .endpoint(properties.getEndpoint())
                    .credentials(properties.getAccessKey(), properties.getSecretKey())
                    .build();
        } catch (Exception e) {
            if (log != null) {
                log.error("Failed to create MinIO client: {}", e.getMessage());
            }
            throw new StorageException("Failed to create MinIO client", e);
        }
    }
}