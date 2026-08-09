package org.datamate.authz.service.policy;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.api.policy.PermissionRepository;
import org.datamate.authz.api.policy.PolicyRepository;
import org.datamate.authz.api.policy.PolicyBundleCacheRepository;
import org.datamate.authz.api.policy.PolicyCompiler;
import org.datamate.authz.api.policy.ConditionFieldRepository;
import org.datamate.authz.model.policy.entity.Permission;
import org.datamate.authz.model.policy.entity.Policy;
import org.datamate.authz.model.policy.entity.ConditionField;
import org.datamate.authz.model.policy.enumtype.Status;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.exception.PolicyCompilationException;

import org.datamate.authz.compiler.generator.RegoGenerator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

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
 * <p>Implements {@link PolicyCompiler} so that dependent use cases
 * (e.g. {@link SavePoliciesService}, {@link org.datamate.authz.rest.startup.StartupScanner})
 * depend only on the port interface, not this concrete class.</p>
 *
 * <h3>Pipeline</h3>
 * <ol>
 *   <li>Load all enabled, non-deleted policies from {@code authz_policy}.</li>
 *   <li>Build a {@code permissionId → code} lookup map (one query, not N).</li>
 *   <li>Parse JSON AST and generate Rego via {@link AstBuilder} and {@link RegoGenerator}.</li>
 *   <li>Package as {@code bundle.tar.gz} via {@link TarGzBundleService} (domain service).</li>
 *   <li>Compute MD5 ETag and upsert into {@code authz_policy_bundle_cache}.</li>
 * </ol>
 */
@RequiredArgsConstructor
@Service
public class DefaultPolicyCompiler implements PolicyCompiler {

    private final PolicyRepository policyPort;
    private final PermissionRepository permissionPort;
    private final PolicyBundleCacheRepository bundleCachePort;
    private final ConditionFieldRepository conditionFieldPort;
    private final org.datamate.authz.api.policy.PolicyValidationPort validationPort;

    private final TarGzBundleService bundleBuilder;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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

        RegoGenerator generator = new RegoGenerator(objectMapper);
        String regoContent = generator.generate(targetNamespace, namespacePolicies, permCodeLookup);
        
        org.datamate.authz.model.policy.valueobject.RegoValidationResult result = validationPort.validate(regoContent);
        if (!result.valid()) {
            throw new PolicyCompilationException("Generated Rego for namespace '" + targetNamespace + "' has syntax errors. Bundle NOT updated.");
        }
        
        String contentHash = computeMd5(regoContent.getBytes());
        String currentEtag = bundleCachePort.getBundle(targetNamespace)
                .map(org.datamate.authz.model.policy.entity.PolicyBundleCache::getEtag)
                .orElse(null);
                
        if (!contentHash.equals(currentEtag)) {
            byte[] bundleBytes;
            try {
                bundleBytes = bundleBuilder.build(targetNamespace, regoContent);
            } catch (IOException e) {
                throw new PolicyCompilationException("Failed to build OPA policy bundle for namespace " + targetNamespace, e);
            }
            bundleCachePort.upsertBundle(targetNamespace, bundleBytes, contentHash);
        }
    }

    private String computeMd5(byte[] data) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("MD5").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new PolicyCompilationException("MD5 algorithm not available", e);
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
            
            if (policy.hasCustomRego()) {
                usesDeprecatedField = customRegoMayUseDeprecatedField(policy.getCustomRegoSnippet(), deprecatedFields);
            } else {
                String json = policy.getExpressionJson();
                if (json != null && !json.trim().isEmpty()) {
                    try {
                        JsonNode root = objectMapper.readTree(json);
                        usesDeprecatedField = hasDeprecatedField(root, deprecatedFields);
                    } catch (Exception e) {
                        // Ignore parse errors here, let RegoGenerator fail or ignore
                    }
                }
            }
            if (policy.isDeprecated() != usesDeprecatedField) {
                policyPort.updateDeprecatedStatus(policy.getId(), usesDeprecatedField);
            }
        }
    }

    private boolean customRegoMayUseDeprecatedField(String regoSnippet, Set<String> deprecatedFields) {
        if (regoSnippet == null) return false;
        for (String field : deprecatedFields) {
            if (regoSnippet.contains("input.resource." + field)) {
                return true;
            }
        }
        return false;
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




