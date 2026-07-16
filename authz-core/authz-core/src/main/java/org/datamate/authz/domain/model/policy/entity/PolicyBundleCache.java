package org.datamate.authz.domain.model.policy.entity;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * The compiled OPA bundle for this application module.
 * There is exactly one active row in {@code authz_policy_bundle_cache} per service database.
 *
 * <p>The bundle is regenerated on-demand whenever policy changes are saved.
 * The {@code etag} is an MD5 hash of the bundle data, used by the OPA sidecar
 * for conditional polling ({@code If-None-Match} header → {@code 304 Not Modified}).</p>
 */
@Getter
public class PolicyBundleCache {
    private final Long id;
    private final String namespace;
    private final byte[] bundleData;
    private final String etag;
    private final LocalDateTime createdAt;

    public PolicyBundleCache(Long id, String namespace, byte[] bundleData, String etag, LocalDateTime createdAt) {
        this.id = id;
        this.namespace = namespace;
        this.bundleData = bundleData;
        this.etag = etag;
        this.createdAt = createdAt;
    }
}


