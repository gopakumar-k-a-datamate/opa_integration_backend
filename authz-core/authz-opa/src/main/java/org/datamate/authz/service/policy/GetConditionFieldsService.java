package org.datamate.authz.service.policy;


import org.datamate.authz.dto.policy.ConditionFieldDto;
import org.datamate.authz.api.policy.ConditionFieldRepositoryPort;
import org.datamate.authz.api.policy.PermissionRepositoryPort;
import org.datamate.authz.model.policy.entity.Permission;
import org.springframework.stereotype.Service;
import org.datamate.authz.application.port.in.GetConditionFieldsUseCase;
import org.springframework.transaction.annotation.Transactional;

import org.datamate.authz.model.policy.entity.ConditionField;

import java.util.List;
import java.util.Optional;

/**
 * Returns ACTIVE condition fields for a permission code, used by the Condition Builder UI.
 */
/* Todo- check exception management
separation of concern
logger if needed
necessity of transaction
 */
@Service
@Transactional(readOnly = true)
public class GetConditionFieldsService implements GetConditionFieldsUseCase {

    private final PermissionRepositoryPort permissionPort;
    private final ConditionFieldRepositoryPort conditionFieldPort;

    public GetConditionFieldsService(PermissionRepositoryPort permissionPort, ConditionFieldRepositoryPort conditionFieldPort) {
        this.permissionPort = permissionPort;
        this.conditionFieldPort = conditionFieldPort;
    }


    public List<ConditionFieldDto> getFields(String permissionCode) {
        Optional<Permission> permission = permissionPort.findByCode(permissionCode);
        if (permission.isEmpty()) {
            return List.of();
        }

        return conditionFieldPort.findAllByPermissionId(permission.get().getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ConditionFieldDto toDto(ConditionField field) {
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
}
