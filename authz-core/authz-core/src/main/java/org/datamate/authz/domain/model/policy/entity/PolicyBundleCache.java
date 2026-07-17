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
    private byte[] bundleData;
    private String etag;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private PolicyBundleCache(Long id, String namespace, byte[] bundleData, String etag, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.namespace = namespace;
        this.bundleData = bundleData;
        this.etag = etag;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PolicyBundleCache create(String namespace, byte[] bundleData, String etag) {
        LocalDateTime now = LocalDateTime.now();
        return new PolicyBundleCache(null, namespace, bundleData, etag, now, now);
    }

    public void updateBundle(byte[] newBundleData, String newEtag) {
        this.bundleData = newBundleData;
        this.etag = newEtag;
        this.updatedAt = LocalDateTime.now();
    }

    public static PolicyBundleCache reconstitute(Long id, String namespace, byte[] bundleData, String etag, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new PolicyBundleCache(id, namespace, bundleData, etag, createdAt, updatedAt);
    }
}


