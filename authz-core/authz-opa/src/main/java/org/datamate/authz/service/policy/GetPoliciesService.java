package org.datamate.authz.service.policy;


import org.datamate.authz.dto.policy.PolicyGridItemDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.datamate.authz.api.policy.PermissionRepository;
import org.datamate.authz.api.policy.PolicyRepository;
import org.datamate.authz.api.policy.ResourceRepository;
import org.datamate.authz.model.policy.entity.Permission;
import org.datamate.authz.model.policy.entity.Policy;
import org.datamate.authz.model.policy.entity.Resource;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Builds the Role-Permission Grid by joining all registered permissions with
 * existing policies for the given subject. A permission with no policy row
 * appears in the grid as unchecked (enabled=false, no effect, no condition).
 */
/* Todo- check exception management
separation of concern
logger if needed
necessity of transaction
 */
@Service
@Transactional(readOnly = true)
public class GetPoliciesService {

    public GetPoliciesService(PermissionRepository permissionPort,
                              ResourceRepository resourcePort,
                              PolicyRepository policyPort,
                              ObjectMapper objectMapper) {
        this.permissionPort = permissionPort;
        this.resourcePort = resourcePort;
        this.policyPort = policyPort;
        this.objectMapper = objectMapper;
    }

    private final PermissionRepository permissionPort;
    private final ResourceRepository resourcePort;
    private final PolicyRepository policyPort;
    private final ObjectMapper objectMapper;
    
    public List<PolicyGridItemDto> getPolicies(SubjectType subjectType, String subjectId, String namespace) {
        // Build resource lookup map filtered by namespace
        Map<Long, Resource> resourcesById = resourcePort.findAllActive()
                .stream()
                .filter(r -> r.getNamespace().equals(namespace))
                .collect(Collectors.toMap(Resource::getId, r -> r));

        // Fetch all permissions and existing policies for this subject
        List<Permission> permissions = permissionPort.findAllActive().stream()
                .filter(p -> resourcesById.containsKey(p.getResourceId()))
                .toList();
        List<Policy> policies = policyPort.findBySubject(subjectType, subjectId);

        // Index policies by permissionId for O(1) lookup
        Map<Long, Policy> policyByPermissionId = policies.stream()
                .collect(Collectors.toMap(Policy::getPermissionId, p -> p));

        List<PolicyGridItemDto> result = new ArrayList<>();
        for (Permission permission : permissions) {
            Resource resource = resourcesById.get(permission.getResourceId());
            if (resource == null) continue;
            Policy policy = policyByPermissionId.get(permission.getId());
            result.add(toDto(permission, resource, policy));
        }

        return result;
    }

    private PolicyGridItemDto toDto(Permission permission, Resource resource, Policy policy) {
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
}
