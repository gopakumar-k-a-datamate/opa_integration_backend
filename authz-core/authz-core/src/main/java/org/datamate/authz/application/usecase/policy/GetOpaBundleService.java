package org.datamate.authz.application.usecase.policy;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.application.dto.policy.BundleResult;
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
    public BundleResult getBundle(String namespace, String ifNoneMatch) {
        Optional<PolicyBundleCache> bundleOpt = bundleCachePort.getBundle(namespace);
        
        if (bundleOpt.isEmpty()) {
            return BundleResult.empty();
        }

        PolicyBundleCache bundle = bundleOpt.get();
        String currentEtag = "\"" + bundle.getEtag() + "\"";

        if (currentEtag.equals(ifNoneMatch)) {
            return BundleResult.notModified(currentEtag);
        }

        return BundleResult.success(bundle.getBundleData(), currentEtag);
    }
}
