package org.datamate.authz.service.policy;

import org.datamate.authz.application.port.out.PermissionRepositoryPort;
import org.datamate.authz.application.port.out.PolicyRepositoryPort;
import org.datamate.authz.application.port.out.PolicyBundleCacheRepositoryPort;
import org.datamate.authz.application.port.out.PolicyCompilerPort;
import org.datamate.authz.application.port.out.ConditionFieldRepositoryPort;
import org.datamate.authz.api.policy.*;
import org.datamate.authz.model.policy.entity.Permission;
import org.datamate.authz.model.policy.entity.Policy;
import org.datamate.authz.model.policy.entity.ConditionField;
import org.datamate.authz.model.policy.enumtype.Status;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
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
 * <p>Implements {@link PolicyCompilerPort} so that dependent use cases
 * (e.g. {@link SavePoliciesService}, {@link org.datamate.authz.rest.startup.StartupScanner})
 * depend only on the port interface, not this concrete class.</p>
 *
 * <h3>Pipeline</h3>
 * <ol>
 *   <li>Load all enabled, non-deleted policies from {@code authz_policy}.</li>
 *   <li>Build a {@code permissionId â†’ code} lookup map (one query, not N).</li>
 *   <li>Parse JSON AST and generate Rego via {@link AstBuilder} and {@link RegoGenerator}.</li>
 *   <li>Package as {@code bundle.tar.gz} via {@link TarGzBundleService} (domain service).</li>
 *   <li>Compute MD5 ETag and upsert into {@code authz_policy_bundle_cache}.</li>
 * </ol>
 */

/* Todo- check exception management
separation of concern
logger if needed
 */
@Service
public class DefaultPolicyCompiler implements PolicyCompilerPort {

    private final PolicyRepositoryPort policyPort;
    private final PermissionRepositoryPort permissionPort;
    private final PolicyBundleCacheRepositoryPort bundleCachePort;
    private final ConditionFieldRepositoryPort conditionFieldPort;
    private final org.datamate.authz.application.port.out.PolicyValidationPort validationPort;


    @EnableLogger
    private Logger log;
    private final TarGzBundleService bundleBuilder;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized void recompile(String targetNamespace) {
        log.info("Initiating OPA policy recompilation for namespace: {}", targetNamespace);
        synchronizeDeprecatedPolicies();

        List<Policy> allEnabledPolicies = policyPort.findAllEnabled();

        // Build permissionId â†’ code lookup (single query â€” no N+1)
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
            String errorMessage = String.format(
                "Generated Rego for namespace '%s' has syntax errors. Bundle NOT updated.\nValidation Errors: %s\nGenerated Rego:\n%s", 
                targetNamespace, 
                result.errors(), 
                regoContent
            );
            log.error("Generated Rego for namespace '{}' has syntax errors. Validation Errors: {}", targetNamespace, result.errors());
            throw new PolicyCompilationException(errorMessage);
        }
        
        String contentHash = computeMd5(regoContent.getBytes());
        String currentEtag = bundleCachePort.getBundle(targetNamespace)
                .map(org.datamate.authz.model.policy.entity.PolicyBundleCache::getEtag)
                .orElse(null);
                
        if (!contentHash.equals(currentEtag)) {
            log.info("Changes detected in generated Rego. Building and caching new OPA bundle for namespace: {}", targetNamespace);
            byte[] bundleBytes;
            try {
                bundleBytes = bundleBuilder.build(targetNamespace, regoContent);
            } catch (IOException e) {
                log.error("Failed to build OPA policy bundle for namespace {}", targetNamespace, e);
                throw new PolicyCompilationException("Failed to build OPA policy bundle for namespace " + targetNamespace, e);
            }
            bundleCachePort.upsertBundle(targetNamespace, bundleBytes, contentHash);
            log.info("Successfully updated OPA bundle cache for namespace: {}", targetNamespace);
        } else {
            log.debug("Generated Rego matches cached version (ETag: {}). No bundle update necessary.", currentEtag);
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




