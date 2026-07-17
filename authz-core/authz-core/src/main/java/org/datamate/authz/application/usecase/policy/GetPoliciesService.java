package org.datamate.authz.application.usecase.policy;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.application.dto.policy.PolicyGridItemDto;
import org.datamate.authz.application.dto.policy.mapper.PolicyDtoMapper;
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
import java.util.stream.Collectors;

/**
 * Builds the Role-Permission Grid by joining all registered permissions with
 * existing policies for the given subject. A permission with no policy row
 * appears in the grid as unchecked (enabled=false, no effect, no condition).
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class GetPoliciesService implements GetPoliciesUseCase {

    private final PermissionPersistencePort permissionPort;
    private final ResourcePersistencePort resourcePort;
    private final PolicyPersistencePort policyPort;
    private final PolicyDtoMapper policyDtoMapper;

    @Override
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
            result.add(policyDtoMapper.toDto(permission, resource, policy));
        }

        return result;
    }
}




