package org.datamate.authz.application.usecase.policy;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.application.port.out.policy.PermissionPersistencePort;
import org.datamate.authz.application.port.out.policy.PolicyPersistencePort;
import org.datamate.authz.application.port.out.policy.PolicyBundleCachePersistencePort;
import org.datamate.authz.application.port.out.policy.PolicyCompilerPort;
import org.datamate.authz.domain.model.policy.entity.Permission;
import org.datamate.authz.domain.model.policy.entity.Policy;

import org.datamate.authz.compiler.generator.RegoGenerator;
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
 *   <li>Parse JSON AST and generate Rego via {@link AstBuilder} and {@link RegoGenerator}.</li>
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

    private final TarGzBundleBuilder bundleBuilder;
    @Override
    @Transactional
    public synchronized void recompile(String targetNamespace) {
        List<Policy> allEnabledPolicies = policyPort.findAllEnabled();

        // Build permissionId → code lookup (single query — no N+1)
        Map<Long, String> permCodeLookup = permissionPort.findAllActive()
                .stream()
                .collect(Collectors.toMap(Permission::getId, Permission::getCode));

        // Filter policies for the specific namespace
        List<Policy> namespacePolicies = allEnabledPolicies.stream()
                .filter(p -> {
                    String code = permCodeLookup.get(p.getPermissionId());
                    return code != null && code.startsWith(targetNamespace + ":");
                })
                .toList();

        RegoGenerator generator = new RegoGenerator();
        String regoContent = generator.generate(targetNamespace, namespacePolicies);
        byte[] bundleBytes;
        try {
            bundleBytes = bundleBuilder.build(regoContent);
        } catch (IOException e) {
            throw new RuntimeException("Failed to build OPA policy bundle for namespace " + targetNamespace, e);
        }
        bundleCachePort.upsertBundle(targetNamespace, bundleBytes, computeMd5(bundleBytes));
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




