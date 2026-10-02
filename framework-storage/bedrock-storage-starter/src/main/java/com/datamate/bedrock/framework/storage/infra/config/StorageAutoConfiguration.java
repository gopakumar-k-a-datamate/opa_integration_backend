package com.datamate.bedrock.framework.storage.infra.config;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import com.datamate.bedrock.framework.storage.adapter.minio.MinioClientConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Storage Auto-Configuration - Infrastructure Layer
 */
@AutoConfiguration
@ComponentScan(basePackages = {
        "com.datamate.bedrock.framework.storage.adapter",
        "com.datamate.bedrock.framework.storage.application"
})
@Import(MinioClientConfig.class)
public class StorageAutoConfiguration {

    @EnableLogger
    private Logger log;

    @PostConstruct
    public void init() {
        if (log != null) {
            log.info("Bedrock Storage auto-configuration initialized successfully");
        }
    }
}
