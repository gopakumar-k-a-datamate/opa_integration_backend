package com.datamate.bedrock.framework.storage.infra.config;

import com.datamate.bedrock.framework.storage.adapter.minio.MinioClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * ═══════════════════════════════════════════════════════════════
 * STORAGE AUTO-CONFIGURATION - Infrastructure Layer
 * ═══════════════════════════════════════════════════════════════
 */
@Slf4j
@AutoConfiguration
@ComponentScan(basePackages = {
        "com.datamate.bedrock.framework.storage.adapter",
        "com.datamate.bedrock.framework.storage.application"
})
@Import(MinioClientConfig.class)
public class StorageAutoConfiguration {

    public StorageAutoConfiguration() {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("🚀 Bedrock Storage Auto-Configuration Started");
        log.info("═══════════════════════════════════════════════════════════");
    }
}
