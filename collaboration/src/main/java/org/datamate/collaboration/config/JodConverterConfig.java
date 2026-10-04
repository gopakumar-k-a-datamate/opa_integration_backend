package org.datamate.collaboration.config;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.remote.RemoteConverter;
import org.jodconverter.remote.office.RemoteOfficeManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration for Remote LibreOffice OfficeManager and JODConverter.
 * <p>
 * Positioned in the {@code org.datamate.collaboration.config} layer to adhere to Hexagonal Architecture
 * and maintain clear separation from outbound adapter implementations.
 * Uses type-safe {@link JodConverterProperties} and Bedrock Logger.
 */
@Configuration
@ConditionalOnProperty(name = "jodconverter.remote.enabled", havingValue = "true")
@EnableConfigurationProperties(JodConverterProperties.class)
@RequiredArgsConstructor
public class JodConverterConfig {

    @EnableLogger
    private Logger log;

    private final JodConverterProperties properties;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RemoteOfficeManager officeManager() {
        if (log != null) {
            log.info("Initializing remote LibreOffice office manager with endpoint [{}]", properties.getUrl());
        }
        return RemoteOfficeManager.builder()
                .urlConnection(properties.getUrl())
                .connectTimeout(properties.getConnectTimeout())
                .socketTimeout(properties.getSocketTimeout())
                .poolSize(properties.getPoolSize())
                .build();
    }

    @Bean
    public DocumentConverter jodConverter(RemoteOfficeManager officeManager) {
        return RemoteConverter.builder()
                .officeManager(officeManager)
                .build();
    }
}