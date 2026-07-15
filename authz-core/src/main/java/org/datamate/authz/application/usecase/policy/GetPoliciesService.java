package org.datamate.authz.application.usecase.policy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.application.dto.policy.PolicyGridItemDto;
import org.datamate.authz.application.port.in.policy.GetPoliciesUseCase;
import org.datamate.authz.application.port.out.policy.PermissionPersistencePort;
import org.datamate.authz.application.port.out.policy.PolicyPersistencePort;
import org.datamate.authz.application.port.out.policy.ResourcePersistencePort;
import org.datamate.authz.domain.model.policy.entity.Permission;
import org.datamate.authz.domain.model.policy.entity.Policy;
import org.datamate.authz.domain.model.policy.entity.Resource;
import org.datamate.authz.domain.model.policy.enumtype.SubjectType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Builds the Role-Permission Grid by joining all registered permissions with
 * existing policies for the given subject. A permission with no policy row
 * appears in the grid as unchecked (enabled=false, no effect, no condition).
 */
@Service
@Transactional(readOnly = true)
public class GetPoliciesService implements GetPoliciesUseCase {

    private final PermissionPersistencePort permissionPort;
    private final ResourcePersistencePort resourcePort;
    private final PolicyPersistencePort policyPort;
    private final ObjectMapper objectMapper;

    public GetPoliciesService(PermissionPersistencePort permissionPort,
                              ResourcePersistencePort resourcePort,
                              PolicyPersistencePort policyPort,
                              ObjectMapper objectMapper) {
        this.permissionPort = permissionPort;
        this.resourcePort = resourcePort;
        this.policyPort = policyPort;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<PolicyGridItemDto> getPolicies(SubjectType subjectType, String subjectId) {
        // Build resource lookup map
        Map<UUID, Resource> resourcesById = resourcePort.findAllActive()
                .stream()
                .collect(Collectors.toMap(Resource::getId, r -> r));

        // Fetch all permissions and existing policies for this subject
        List<Permission> permissions = permissionPort.findAllActive();
        List<Policy> policies = policyPort.findBySubject(subjectType, subjectId);

        // Index policies by permissionId for O(1) lookup
        Map<UUID, Policy> policyByPermissionId = policies.stream()
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

    private PolicyGridItemDto toDto(Permission permission, Resource resource,
                                    Policy policy) {
        if (policy == null) {
            return new PolicyGridItemDto(
                    permission.getCode(),
                    permission.getAction(),
                    resource.getNamespace(),
                    resource.getName(),
                    null, null, null, false, null
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
                policy.getDisabledReason()
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


