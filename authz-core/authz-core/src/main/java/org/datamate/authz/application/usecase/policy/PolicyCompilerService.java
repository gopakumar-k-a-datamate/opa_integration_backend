package org.datamate.authz.application.usecase.policy;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.application.port.out.policy.PermissionPersistencePort;
import org.datamate.authz.application.port.out.policy.PolicyPersistencePort;
import org.datamate.authz.application.port.out.policy.PolicyBundleCachePersistencePort;
import org.datamate.authz.application.port.out.policy.PolicyCompilerPort;
import org.datamate.authz.application.port.out.policy.ConditionFieldPersistencePort;
import org.datamate.authz.domain.model.policy.entity.Permission;
import org.datamate.authz.domain.model.policy.entity.Policy;
import org.datamate.authz.domain.model.policy.entity.ConditionField;
import org.datamate.authz.domain.model.policy.enumtype.Status;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.util.Set;
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
    private final ConditionFieldPersistencePort conditionFieldPort;

    private final TarGzBundleBuilder bundleBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public synchronized void recompile(String targetNamespace) {
        synchronizeDeprecatedPolicies();

        List<Policy> allEnabledPolicies = policyPort.findAllEnabled();

        // Build permissionId → code lookup (single query — no N+1)
        Map<Long, String> permCodeLookup = permissionPort.findAllActive()
                .stream()
                .filter(p -> p.getStatus() == Status.ACTIVE)
                .collect(Collectors.toMap(Permission::getId, Permission::getCode));

        // Filter policies for the specific namespace, excluding deprecated ones
        List<Policy> namespacePolicies = allEnabledPolicies.stream()
                .filter(p -> !p.isDeprecated())
                .filter(p -> {
                    String code = permCodeLookup.get(p.getPermissionId());
                    return code != null && code.startsWith(targetNamespace + ":");
                })
                .toList();

        RegoGenerator generator = new RegoGenerator();
        String regoContent = generator.generate(targetNamespace, namespacePolicies, permCodeLookup);
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

    private void synchronizeDeprecatedPolicies() {
        Set<String> deprecatedFields = conditionFieldPort.findAllDeprecated()
                .stream()
                .map(ConditionField::getFieldName)
                .collect(Collectors.toSet());

        List<Policy> activePolicies = policyPort.findAllActive();
        for (Policy policy : activePolicies) {
            boolean usesDeprecatedField = false;
            String json = policy.getExpressionJson();
            if (json != null && !json.trim().isEmpty()) {
                try {
                    JsonNode root = objectMapper.readTree(json);
                    usesDeprecatedField = hasDeprecatedField(root, deprecatedFields);
                } catch (Exception e) {
                    // Ignore parse errors here, let RegoGenerator fail or ignore
                }
            }
            if (policy.isDeprecated() != usesDeprecatedField) {
                policyPort.updateDeprecatedStatus(policy.getId(), usesDeprecatedField);
            }
        }
    }

    private boolean hasDeprecatedField(JsonNode node, Set<String> deprecatedFields) {
        if (node == null || node.isMissingNode()) return false;
        if (node.has("field")) {
            String field = node.get("field").asText();
            if (deprecatedFields.contains(field)) {
                return true;
            }
        }
        if (node.has("children") && node.get("children").isArray()) {
            for (JsonNode child : node.get("children")) {
                if (hasDeprecatedField(child, deprecatedFields)) {
                    return true;
                }
            }
        }
        return false;
    }
}




