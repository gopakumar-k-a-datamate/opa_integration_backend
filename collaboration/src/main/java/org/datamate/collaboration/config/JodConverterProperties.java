package org.datamate.collaboration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for JODConverter / LibreOffice remote conversion.
 * Follows Spring Boot 12-factor configuration principles and avoids hardcoded URLs in annotations.
 */
@Data
@ConfigurationProperties(prefix = "jodconverter.remote")
public class JodConverterProperties {

    /**
     * Whether remote LibreOffice / JODConverter integration is enabled.
     */
    private boolean enabled = true;

    /**
     * Remote LibreOffice service endpoint URL.
     * Configured via application.properties or environment variables (e.g., JODCONVERTER_REMOTE_URL).
     */
    private String url;

    /**
     * Socket timeout in milliseconds for remote connection.
     */
    private long socketTimeout = 60000;

    /**
     * Connect timeout in milliseconds for remote connection.
     */
    private long connectTimeout = 5000;

    /**
     * Size of the remote LibreOffice connection pool.
     */
    private int poolSize = 5;
}