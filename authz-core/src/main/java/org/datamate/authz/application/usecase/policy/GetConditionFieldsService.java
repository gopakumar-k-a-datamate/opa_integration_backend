package org.datamate.authz.application.usecase.policy;

import org.datamate.authz.application.dto.policy.ConditionFieldDto;
import org.datamate.authz.application.port.in.policy.GetConditionFieldsUseCase;
import org.datamate.authz.application.port.out.policy.ConditionFieldPersistencePort;
import org.datamate.authz.application.port.out.policy.PermissionPersistencePort;
import org.datamate.authz.domain.model.policy.entity.ConditionField;
import org.datamate.authz.domain.model.policy.entity.Permission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Returns ACTIVE condition fields for a permission code, used by the Condition Builder UI.
 */
@Service
@Transactional(readOnly = true)
public class GetConditionFieldsService implements GetConditionFieldsUseCase {

    private final PermissionPersistencePort permissionPort;
    private final ConditionFieldPersistencePort conditionFieldPort;

    public GetConditionFieldsService(PermissionPersistencePort permissionPort,
                                     ConditionFieldPersistencePort conditionFieldPort) {
        this.permissionPort = permissionPort;
        this.conditionFieldPort = conditionFieldPort;
    }

    @Override
    public List<ConditionFieldDto> getFields(String permissionCode) {
        Optional<Permission> permission = permissionPort.findByCode(permissionCode);
        if (permission.isEmpty()) {
            return List.of();
        }

        return conditionFieldPort.findActiveByPermissionId(permission.get().getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ConditionFieldDto toDto(ConditionField field) {
        return new ConditionFieldDto(
                field.getFieldName(),
                field.getFieldType(),
                field.getDisplayName(),
                field.getAllowedValues(),
                field.getOptionsEndpoint()
        );
    }
}


