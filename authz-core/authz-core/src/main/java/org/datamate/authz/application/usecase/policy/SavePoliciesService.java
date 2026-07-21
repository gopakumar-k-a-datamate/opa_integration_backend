package org.datamate.authz.application.usecase.policy;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.application.dto.policy.PolicyItemRequest;
import org.datamate.authz.application.dto.policy.SavePoliciesRequest;
import org.datamate.authz.application.port.in.policy.SavePoliciesUseCase;
import org.datamate.authz.application.port.out.policy.PermissionPersistencePort;
import org.datamate.authz.application.port.out.policy.PolicyPersistencePort;
import org.datamate.authz.application.port.out.policy.PolicyCompilerPort;
import org.datamate.authz.domain.model.policy.entity.Permission;
import org.datamate.authz.domain.model.policy.entity.Policy;
import org.datamate.authz.domain.model.policy.enumtype.SubjectType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.datamate.authz.shared.exception.InvalidPayloadException;

/**
 * Full-state sync for all policies belonging to a subject within this module.
 *
 * <p>Algorithm:
 * <ol>
 *   <li>Load all existing non-deleted policies for this subject.</li>
 *   <li>For each incoming item with {@code isDeleted=true} → soft-delete the matching policy.</li>
 *   <li>For each other incoming item → upsert the policy (insert or update).</li>
 *   <li>Soft-delete any existing DB policies whose permissionCode is absent from the payload.</li>
 *   <li>Trigger the {@link PolicyCompilerPort} to regenerate the OPA bundle.</li>
 * </ol>
 * </p>
 */
@RequiredArgsConstructor
@Service
public class SavePoliciesService implements SavePoliciesUseCase {

    private final PolicyPersistencePort policyPort;
    private final PermissionPersistencePort permissionPort;
    private final PolicyCompilerPort compilerPort;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void savePolicies(SavePoliciesRequest request) {
        if (request == null || request.policies() == null) {
            throw new InvalidPayloadException("The 'policies' array in the request body cannot be null.");
        }

        SubjectType subjectType = request.subjectType();
        String subjectId = request.subjectId();
        String targetNamespace = request.namespace();

        // Load existing active policies for this subject
        List<Policy> allExisting = policyPort.findBySubject(subjectType, subjectId);

        // Cache all permissions to prevent N+1 queries during resolution
        List<Permission> allPermissions = permissionPort.findAllActive();
        Map<Long, String> permissionCodeById = allPermissions.stream()
                .collect(Collectors.toMap(Permission::getId, Permission::getCode));
        Map<String, Permission> permissionByCode = allPermissions.stream()
                .collect(Collectors.toMap(Permission::getCode, p -> p));

        // Filter existing policies to ONLY those in the target namespace
        List<Policy> existingInNamespace = allExisting.stream()
                .filter(p -> {
                    String code = permissionCodeById.get(p.getPermissionId());
                    return code != null && code.startsWith(targetNamespace + ":");
                })
                .toList();

        Map<Long, Policy> existingByPermissionId = existingInNamespace.stream()
                .collect(Collectors.toMap(Policy::getPermissionId, p -> p));

        // Track which permissionIds are explicitly in the payload
        Set<String> handledCodes = request.policies().stream()
                .map(PolicyItemRequest::permissionCode)
                .collect(Collectors.toSet());

        for (PolicyItemRequest item : request.policies()) {
            if (item.isDeleted() && (item.deletedReason() == null || item.deletedReason().isBlank())) {
                throw new InvalidPayloadException("A reason is mandatory when deleting a policy (permissionCode: " + item.permissionCode() + ").");
            }
            if (!item.enabled() && !item.isDeleted() && (item.disabledReason() == null || item.disabledReason().isBlank())) {
                throw new InvalidPayloadException("A reason is mandatory when disabling a policy (permissionCode: " + item.permissionCode() + ").");
            }

            Permission permission = permissionByCode.get(item.permissionCode());
            if (permission == null) continue; // unknown permission code — skip
            Policy existingPolicy = existingByPermissionId.get(permission.getId());

            if (item.isDeleted()) {
                if (existingPolicy != null) {
                    policyPort.softDelete(existingPolicy.getId(), item.deletedReason());
                }
            } else {
                String expressionJson = serializeJson(item);
                Long policyId = (existingPolicy != null) ? existingPolicy.getId() : null;
                policyPort.upsert(
                        policyId,
                        permission.getId(),
                        subjectType,
                        subjectId,
                        item.effect(),
                        expressionJson,
                        item.enabled(),
                        item.disabledReason()
                );
            }
        }

        // Soft-delete existing policies whose permissionCode is missing from payload
        for (Map.Entry<Long, Policy> entry : existingByPermissionId.entrySet()) {
            String code = permissionCodeById.get(entry.getKey());
            boolean notInPayload = code == null || !handledCodes.contains(code);
            if (notInPayload) {
                policyPort.softDelete(entry.getValue().getId(), "Removed policy in state sync.");
            }
        }

        // Regenerate the OPA bundle via the application port (DIP)
        compilerPort.recompile(targetNamespace);
    }

    private String serializeJson(PolicyItemRequest item) {
        if (item.expressionJson() == null) return null;
        try {
            return objectMapper.writeValueAsString(item.expressionJson());
        } catch (Exception e) {
            return null;
        }
    }
}





