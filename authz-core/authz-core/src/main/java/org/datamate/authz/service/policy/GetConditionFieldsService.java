package org.datamate.authz.service.policy;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.dto.policy.ConditionFieldDto;
import org.datamate.authz.api.policy.ConditionFieldRepository;
import org.datamate.authz.api.policy.PermissionRepository;
import org.datamate.authz.model.policy.entity.Permission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.datamate.authz.dto.policy.mapper.ConditionFieldDtoMapper;

import java.util.List;
import java.util.Optional;

/**
 * Returns ACTIVE condition fields for a permission code, used by the Condition Builder UI.
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class GetConditionFieldsService {

    private final PermissionRepository permissionPort;
    private final ConditionFieldRepository conditionFieldPort;
    private final ConditionFieldDtoMapper conditionFieldDtoMapper;

    
    public List<ConditionFieldDto> getFields(String permissionCode) {
        Optional<Permission> permission = permissionPort.findByCode(permissionCode);
        if (permission.isEmpty()) {
            return List.of();
        }

        return conditionFieldPort.findAllByPermissionId(permission.get().getId())
                .stream()
                .map(conditionFieldDtoMapper::toDto)
                .toList();
    }
}




