package org.datamate.authz.application.port.out.policy;

import org.datamate.authz.domain.model.policy.entity.PolicyBundleCache;

import java.util.Optional;

/** Persistence operations for {@code authz_policy_bundle_cache}. */
public interface PolicyBundleCachePersistencePort {

    /**
     * Returns the current compiled bundle.
     * There is exactly one row per service database.
     */
    Optional<PolicyBundleCache> getBundle();

    /**
     * Insert or update the single bundle row.
     * Generates a new ID if no row exists; updates in-place otherwise.
     */
    PolicyBundleCache upsertBundle(byte[] bundleData, String etag);
}


