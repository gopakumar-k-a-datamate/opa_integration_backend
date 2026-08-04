package org.datamate.authz.service.policy;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.dto.policy.PolicyGridItemDto;
import org.datamate.authz.dto.policy.mapper.PolicyDtoMapper;
import org.datamate.authz.service.policy.GetPoliciesService;
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
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class GetPoliciesService {

    private final PermissionRepository permissionPort;
    private final ResourceRepository resourcePort;
    private final PolicyRepository policyPort;
    private final PolicyDtoMapper policyDtoMapper;

    
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




