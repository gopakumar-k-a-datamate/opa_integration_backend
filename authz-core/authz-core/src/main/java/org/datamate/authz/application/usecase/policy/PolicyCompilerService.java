package org.datamate.authz.application.usecase.policy;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.application.port.out.policy.PermissionPersistencePort;
import org.datamate.authz.application.port.out.policy.PolicyPersistencePort;
import org.datamate.authz.application.port.out.policy.PolicyBundleCachePersistencePort;
import org.datamate.authz.application.port.out.policy.PolicyCompilerPort;
import org.datamate.authz.domain.model.policy.entity.Permission;
import org.datamate.authz.domain.model.policy.entity.Policy;
import org.datamate.authz.domain.service.policy.RegoGenerator;
import org.datamate.authz.domain.service.policy.TarGzBundleBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Application use case that orchestrates the OPA policy compilation pipeline.
 *
 * <p>Implements {@link PolicyCompilerPort} so that dependent use cases
 * (e.g. {@link SavePoliciesService}, {@link org.datamate.authz.adapter.in.startup.StartupScanner})
 * depend only on the port interface, not this concrete class.</p>
 *
 * <h3>Pipeline</h3>
 * <ol>
 *   <li>Load all enabled, non-deleted policies from {@code authz_policy}.</li>
 *   <li>Build a {@code permissionId → code} lookup map (one query, not N).</li>
 *   <li>Generate Rego via {@link RegoGenerator} (domain service — pure computation).</li>
 *   <li>Package as {@code bundle.tar.gz} via {@link TarGzBundleBuilder} (domain service).</li>
 *   <li>Compute MD5 ETag and upsert into {@code authz_policy_bundle_cache}.</li>
 * </ol>
 */
@RequiredArgsConstructor
@Service
public class PolicyCompilerService implements PolicyCompilerPort {

    private final PolicyPersistencePort policyPort;
    private final PermissionPersistencePort permissionPort;
    private final PolicyBundleCachePersistencePort bundleCachePort;
    private final RegoGenerator regoGenerator;
    private final TarGzBundleBuilder bundleBuilder;
@Override
    @Transactional
    public synchronized void recompile() {
        List<Policy> enabledPolicies = policyPort.findAllEnabled();

        // Build permissionId → code lookup (single query — no N+1)
        Map<Long, String> permCodeLookup = permissionPort.findAllActive()
                .stream()
                .collect(Collectors.toMap(Permission::getId, Permission::getCode));

        // Group policies by namespace (extracted from permission code "{namespace}:{resource}:{action}")
        Map<String, List<Policy>> policiesByNamespace = enabledPolicies.stream()
                .collect(Collectors.groupingBy(p -> {
                    String code = permCodeLookup.get(p.getPermissionId());
                    if (code == null) return "default";
                    return code.split(":")[0];
                }));

        for (Map.Entry<String, List<Policy>> entry : policiesByNamespace.entrySet()) {
            String namespace = entry.getKey();
            List<Policy> namespacePolicies = entry.getValue();
            
            String regoContent = regoGenerator.generate(namespacePolicies, permCodeLookup);
            byte[] bundleBytes;
            try {
                bundleBytes = bundleBuilder.build(regoContent);
            } catch (IOException e) {
                throw new RuntimeException("Failed to build OPA policy bundle for namespace " + namespace, e);
            }
            bundleCachePort.upsertBundle(namespace, bundleBytes, computeMd5(bundleBytes));
        }
    }

    private String computeMd5(byte[] data) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("MD5").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
}




