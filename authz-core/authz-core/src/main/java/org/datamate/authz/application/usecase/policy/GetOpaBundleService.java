package org.datamate.authz.application.usecase.policy;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.application.port.in.policy.GetOpaBundleUseCase;
import org.datamate.authz.application.port.out.policy.PolicyBundleCachePersistencePort;
import org.datamate.authz.domain.model.policy.entity.PolicyBundleCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Fetches the current compiled OPA bundle from the local {@code authz_policy_bundle_cache} table.
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class GetOpaBundleService implements GetOpaBundleUseCase {

    private final PolicyBundleCachePersistencePort bundleCachePort;

    @Override
    public Optional<PolicyBundleCache> getBundle(String namespace) {
        return bundleCachePort.getBundle(namespace);
    }
}
