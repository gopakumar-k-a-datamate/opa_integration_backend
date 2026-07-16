package org.datamate.authz.application.port.out.policy;

import org.datamate.authz.domain.model.policy.entity.PolicyBundleCache;

import java.util.Optional;

/** Persistence operations for {@code authz_policy_bundle_cache}. */
public interface PolicyBundleCachePersistencePort {

    /**
     * Returns the current compiled bundle.
     * There is exactly one row per namespace.
     */
    Optional<PolicyBundleCache> getBundle(String namespace);

    /**
     * Insert or update the single bundle row.
     * Generates a new ID if no row exists; updates in-place otherwise.
     */
    PolicyBundleCache upsertBundle(String namespace, byte[] bundleData, String etag);
}


