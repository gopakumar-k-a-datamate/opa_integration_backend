package org.datamate.authz.service.policy;


import org.datamate.authz.dto.policy.BundleResult;
import org.datamate.authz.jpa.repository.PolicyBundleCacheRepository;
import org.datamate.authz.model.policy.entity.PolicyBundleCache;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import org.datamate.authz.api.policy.PolicyCompiler;

/**
 * Fetches the current compiled OPA bundle from the local {@code authz_policy_bundle_cache} table.
 */
/* Todo- check exception management
separation of concern
logger if needed
necessity of transaction
 */
@Service
@Transactional(readOnly = true)
public class GetOpaBundleService {

    private final PolicyBundleCacheRepository bundleCache;
    private final PolicyCompiler compiler;
    
    private final ReentrantLock lock = new ReentrantLock();

    @EnableLogger
    private Logger log;

    public GetOpaBundleService(PolicyBundleCacheRepository bundleCache, PolicyCompiler compiler) {
        this.bundleCache = bundleCache;
        this.compiler = compiler;
    }


    public BundleResult getBundle(String namespace, String ifNoneMatch) {
        log.debug("Fetching OPA bundle for namespace: {}, If-None-Match: {}", namespace, ifNoneMatch);
        Optional<PolicyBundleCache> bundleOpt = bundleCache.getBundle(namespace);
        
        if (bundleOpt.isEmpty() || bundleOpt.get().getEtag() == null || bundleOpt.get().getBundleData() == null) {
            lock.lock();
            try {
                // Double-checked locking
                bundleOpt = bundleCache.getBundle(namespace);
                if (bundleOpt.isEmpty() || bundleOpt.get().getEtag() == null || bundleOpt.get().getBundleData() == null) {
                    log.info("OPA bundle for namespace '{}' not found or incomplete. Triggering synchronous recompilation.", namespace);
                    compiler.recompile(namespace);
                    bundleOpt = bundleCache.getBundle(namespace);
                }
            } finally {
                lock.unlock();
            }
        }

        if (bundleOpt.isEmpty()) {
            log.warn("Failed to retrieve or compile OPA bundle for namespace '{}'. Returning empty result.", namespace);
            return BundleResult.empty();
        }

        PolicyBundleCache bundle = bundleOpt.get();
        String currentEtag = "\"" + bundle.getEtag() + "\"";

        if (currentEtag.equals(ifNoneMatch)) {
            log.debug("OPA bundle for namespace '{}' has not been modified (ETag: {}).", namespace, currentEtag);
            return BundleResult.notModified(currentEtag);
        }

        log.debug("Returning OPA bundle for namespace '{}' (ETag: {}).", namespace, currentEtag);
        return BundleResult.success(bundle.getBundleData(), currentEtag);
    }
}
