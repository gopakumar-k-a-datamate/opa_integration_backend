package org.datamate.authz.application.port.in.policy;

import org.datamate.authz.application.dto.policy.BundleResult;

/**
 * Fetches the current compiled OPA bundle from the local cache.
 */
public interface GetOpaBundleUseCase {
    BundleResult getBundle(String namespace, String ifNoneMatch);
}
