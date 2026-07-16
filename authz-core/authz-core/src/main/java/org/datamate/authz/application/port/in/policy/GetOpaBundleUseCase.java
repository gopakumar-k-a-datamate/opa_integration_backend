package org.datamate.authz.application.port.in.policy;

import org.datamate.authz.domain.model.policy.entity.PolicyBundleCache;

import java.util.Optional;

/**
 * Fetches the current compiled OPA bundle from the local cache.
 * Returns {@link Optional#empty()} if no bundle has been compiled yet.
 */
public interface GetOpaBundleUseCase {
    Optional<PolicyBundleCache> getBundle();
}


