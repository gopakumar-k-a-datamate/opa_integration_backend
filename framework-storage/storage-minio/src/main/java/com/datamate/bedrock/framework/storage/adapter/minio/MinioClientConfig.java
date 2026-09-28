package com.datamate.bedrock.framework.storage.adapter.minio;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * ═══════════════════════════════════════════════════════════════
 * MINIO CLIENT CONFIGURATION - Adapter Layer
 * ═══════════════════════════════════════════════════════════════
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@RequiredArgsConstructor
public class MinioClientConfig {

    private final MinioProperties properties;

    @Bean
    public MinioClient minioClient() {
        log.info("Creating MinIO client...");
        try {
            return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
        } catch (Exception e) {
            log.error("❌ Failed to create MinIO client", e);
            throw new RuntimeException("Failed to create MinIO client", e);
        }
    }
}
