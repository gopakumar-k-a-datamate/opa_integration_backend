package org.datamate.authz.service.policy;

import com.datamate.bedrock.framework.common.auditing.annotation.AuditLog;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.api.policy.ConditionFieldRepository;
import org.datamate.authz.api.policy.PermissionRepository;
import org.datamate.authz.compiler.AstBuilder;
import org.datamate.authz.jpa.repository.PolicyBundleCacheRepository;
import org.datamate.authz.api.policy.PolicyRepository;
import org.datamate.authz.api.policy.PolicyValidation;
import org.datamate.authz.api.policy.ResourceRepository;
import org.datamate.authz.compiler.AstBuilder;
import org.datamate.authz.dto.policy.ConditionFieldDto;
import org.datamate.authz.dto.policy.PolicyGridItemDto;
import org.datamate.authz.exception.AuthzInvalidPayloadException;
import org.datamate.authz.exception.AuthzInvalidSyntaxException;
import org.datamate.authz.model.policy.entity.ConditionField;
import org.datamate.authz.model.policy.entity.Permission;
import org.datamate.authz.model.policy.entity.Policy;
import org.datamate.authz.model.policy.entity.Resource;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.datamate.authz.model.policy.valueobject.RegoValidationResult;
import org.datamate.authz.rest.dto.PolicyItemRequest;
import org.datamate.authz.rest.dto.SavePoliciesRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Standard N-Tier Service implementation for Policy Management.
 * Consolidates all CRUD operations and metadata fetches for policies.
 */
@Service
public class DefaultPolicyManagementService implements PolicyManagementService {

    private final PermissionRepository permissionRepository;
    private final ConditionFieldRepository conditionFieldRepository;
    private final ResourceRepository resourceRepository;
    private final PolicyRepository policyRepository;
    private final PolicyBundleCacheRepository bundleCacheRepository;
    private final PolicyValidation validation;
    private final ObjectMapper objectMapper;
    private final AstBuilder astBuilder;

    @EnableLogger
    private Logger log;

    public DefaultPolicyManagementService(
            PermissionRepository permissionRepository,
            ConditionFieldRepository conditionFieldRepository,
            ResourceRepository resourceRepository,
            PolicyRepository policyRepository,
            PolicyBundleCacheRepository bundleCacheRepository,
            PolicyValidation validation,
            ObjectMapper objectMapper,
            AstBuilder astBuilder) {
        this.permissionRepository = permissionRepository;
        this.conditionFieldRepository = conditionFieldRepository;
        this.resourceRepository = resourceRepository;
        this.policyRepository = policyRepository;
        this.bundleCacheRepository = bundleCacheRepository;
        this.validation = validation;
        this.objectMapper = objectMapper;
        this.astBuilder = astBuilder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConditionFieldDto> getConditionFields(String permissionCode) {
        Optional<Permission> permission = permissionRepository.findByCode(permissionCode);
        if (permission.isEmpty()) {
            return List.of();
        }

        return conditionFieldRepository.findAllByPermissionId(permission.get().getId())
                .stream()
                .map(this::toConditionFieldDto)
                .toList();
    }

    private ConditionFieldDto toConditionFieldDto(ConditionField field) {
        if (field == null) return null;
        return new ConditionFieldDto(
                field.getFieldName(),
                field.getFieldType(),
                field.getDisplayName(),
                field.getAllowedValues(),
                field.getOptionsEndpoint(),
                field.getStatus()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getNamespaces() {
        return resourceRepository.findAllActive()
                .stream()
                .map(Resource::getNamespace)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicyGridItemDto> getPolicies(SubjectType subjectType, String subjectId, String namespace) {
        Map<Long, Resource> resourcesById = resourceRepository.findAllActive()
                .stream()
                .filter(r -> r.getNamespace().equals(namespace))
                .collect(Collectors.toMap(Resource::getId, r -> r));

        List<Permission> permissions = permissionRepository.findAllActive().stream()
                .filter(p -> resourcesById.containsKey(p.getResourceId()))
                .toList();
        List<Policy> policies = policyRepository.findBySubject(subjectType, subjectId);

        Map<Long, Policy> policyByPermissionId = policies.stream()
                .collect(Collectors.toMap(Policy::getPermissionId, p -> p));

        List<PolicyGridItemDto> result = new ArrayList<>();
        for (Permission permission : permissions) {
            Resource resource = resourcesById.get(permission.getResourceId());
            if (resource == null) continue;
            Policy policy = policyByPermissionId.get(permission.getId());
            result.add(toPolicyGridItemDto(permission, resource, policy));
        }

        return result;
    }

    private PolicyGridItemDto toPolicyGridItemDto(Permission permission, Resource resource, Policy policy) {
        if (policy == null) {
            return new PolicyGridItemDto(
                    permission.getCode(),
                    permission.getAction(),
                    resource.getNamespace(),
                    resource.getName(),
                    null, null, null, false, null, null, false, false, null
            );
        }

        JsonNode expressionNode = parseJson(policy.getExpressionJson());
        return new PolicyGridItemDto(
                permission.getCode(),
                permission.getAction(),
                resource.getNamespace(),
                resource.getName(),
                policy.getId(),
                policy.getEffect(),
                expressionNode,
                policy.isEnabled(),
                policy.getDisabledReason(),
                policy.getDeletedReason(),
                policy.isDeprecated(),
                policy.isUseCustomRego(),
                policy.getCustomRegoSnippet()
        );
    }

    private JsonNode parseJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional
    @AuditLog(action = "SAVE_POLICIES", resource = "POLICY", includeArgs = true, description = "Save bulk policies")
    public void savePolicies(SavePoliciesRequest request) {
        SubjectType subjectType = request.subjectType();
        String subjectId = request.subjectId();
        String targetNamespace = request.namespace();

        log.info("Processing SavePoliciesRequest for Subject: [{} {}], Namespace: '{}'", subjectType, subjectId, targetNamespace);

        List<Policy> allExisting = policyRepository.findBySubject(subjectType, subjectId);
        List<Permission> allPermissions = permissionRepository.findAllActive();
        Map<Long, String> permissionCodeById = allPermissions.stream()
                .collect(Collectors.toMap(Permission::getId, Permission::getCode));
        Map<String, Permission> permissionByCode = allPermissions.stream()
                .collect(Collectors.toMap(Permission::getCode, p -> p));

        List<Policy> existingInNamespace = allExisting.stream()
                .filter(p -> {
                    String code = permissionCodeById.get(p.getPermissionId());
                    return code != null && code.startsWith(targetNamespace + ":");
                })
                .toList();

        Map<Long, Policy> existingByPermissionId = existingInNamespace.stream()
                .collect(Collectors.toMap(Policy::getPermissionId, p -> p));

        Set<String> handledCodes = request.policies().stream()
                .map(PolicyItemRequest::permissionCode)
                .collect(Collectors.toSet());

        for (PolicyItemRequest item : request.policies()) {
            if (item.isDeleted() && (item.deletedReason() == null || item.deletedReason().isBlank())) {
                throw new AuthzInvalidPayloadException("A reason is mandatory when deleting a policy (permissionCode: " + item.permissionCode() + ").");
            }
            if (!item.enabled() && !item.isDeleted() && (item.disabledReason() == null || item.disabledReason().isBlank())) {
                throw new AuthzInvalidPayloadException("A reason is mandatory when disabling a policy (permissionCode: " + item.permissionCode() + ").");
            }

            Permission permission = permissionByCode.get(item.permissionCode());
            if (permission == null) continue;
            Policy existingPolicy = existingByPermissionId.get(permission.getId());

            if (item.isDeleted()) {
                if (existingPolicy != null) {
                    log.info("Soft-deleting policy for permissionCode: {} (ID: {}) due to explicit deletion request", item.permissionCode(), existingPolicy.getId());
                    policyRepository.softDelete(existingPolicy.getId(), item.deletedReason());
                }
            } else {
                if (item.useCustomRego() && item.customRegoSnippet() != null && !item.customRegoSnippet().isBlank()) {
                    String snippet = item.customRegoSnippet();
                    
                    // Reject boilerplate keywords in custom snippets to prevent conflicting with the compiler
                    for (String line : snippet.split("\n")) {
                        String trimmed = line.trim();
                        if (trimmed.startsWith("package ") || trimmed.startsWith("import ") || trimmed.startsWith("default ")) {
                            String keyword = trimmed.substring(0, trimmed.indexOf(' '));
                            throw new AuthzInvalidPayloadException(
                                "Custom Rego snippet for " + item.permissionCode() + 
                                " must not contain '" + keyword + "' statements. " +
                                "These are managed automatically by the compiler."
                            );
                        }
                        if (trimmed.startsWith("allow if") || trimmed.startsWith("allow {") || trimmed.startsWith("allow=")) {
                            throw new AuthzInvalidPayloadException(
                                "Custom Rego snippet for " + item.permissionCode() + 
                                " must not define the 'allow' rule. " +
                                "Use 'allow_rule if { ... }' or 'deny_rule if { ... }' instead."
                            );
                        }
                    }

                    RegoValidationResult result = validation.validate(snippet);
                    if (!result.valid()) {
                        throw new AuthzInvalidSyntaxException(item.permissionCode(), result.errors());
                    }
                } else if (!item.useCustomRego() && item.expressionJson() != null && !item.expressionJson().isNull()) {
                    try {
                        astBuilder.build(item.expressionJson());
                    } catch (AuthzInvalidPayloadException e) {
                        throw new AuthzInvalidPayloadException("Validation failed for " + item.permissionCode() + ": " + e.getMessage());
                    }
                }
                
                String expressionJson = serializeJson(item);
                Long policyId = (existingPolicy != null) ? existingPolicy.getId() : null;
                log.debug("Upserting policy for permissionCode: {} (ID: {})", item.permissionCode(), policyId);
                policyRepository.upsert(
                        policyId,
                        permission.getId(),
                        subjectType,
                        subjectId,
                        item.effect(),
                        expressionJson,
                        item.enabled(),
                        item.disabledReason(),
                        item.useCustomRego(),
                        item.customRegoSnippet()
                );
            }
        }

        for (Map.Entry<Long, Policy> entry : existingByPermissionId.entrySet()) {
            String code = permissionCodeById.get(entry.getKey());
            boolean notInPayload = code == null || !handledCodes.contains(code);
            if (notInPayload) {
                log.info("Soft-deleting existing policy for permissionCode: {} (ID: {}) because it is absent from the incoming payload", code, entry.getValue().getId());
                policyRepository.softDelete(entry.getValue().getId(), "Removed policy in state sync.");
            }
        }

        bundleCacheRepository.upsertBundle(targetNamespace, null, null);
    }

    private String serializeJson(PolicyItemRequest item) {
        if (item.expressionJson() == null || item.expressionJson().isNull()) return null;
        try {
            return objectMapper.writeValueAsString(item.expressionJson());
        } catch (Exception e) {
            throw new AuthzInvalidPayloadException("Failed to serialize policy expression AST for permission: " + item.permissionCode());
        }
    }
}
