package org.datamate.authz.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the OPA bundle endpoint auto-configuration.
 *
 * <p>
 * Prefix: {@code datamate.authz.bundle}
 * </p>
 *
 * <ul>
 *   <li>{@code enabled} – Master switch to activate the {@code BundleController} (default: {@code true}).</li>
 *   <li>{@code api-key} – Shared secret the OPA sidecar must send as {@code Authorization: Bearer <key>}.
 *       When {@code null} or blank, no token validation is performed (suitable for localhost-only sidecars).</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "datamate.authz.bundle")
public class BundleProperties {

    /**
     * Whether to auto-activate the bundle endpoint.
     * Set to {@code false} to keep the {@code BundleController} dormant.
     */
    private boolean enabled = true;

    /**
     * Shared API key for authenticating OPA sidecar bundle requests.
     * <p>
     * When set, the sidecar must include an {@code Authorization: Bearer <api-key>}
     * header in every bundle poll request. Requests without a valid token are rejected
     * with {@code 403 Forbidden}.
     * </p>
     * <p>
     * When {@code null} or blank, the bundle endpoint is open — suitable for
     * deployments where the OPA sidecar runs on {@code localhost} within the same
     * pod or machine.
     * </p>
     */
    private String apiKey;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
