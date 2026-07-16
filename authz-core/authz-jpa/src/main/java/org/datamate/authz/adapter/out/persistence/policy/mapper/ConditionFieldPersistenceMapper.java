package org.datamate.authz.adapter.out.persistence.policy.mapper;

import lombok.RequiredArgsConstructor;
import org.datamate.authz.adapter.out.persistence.policy.entity.ConditionFieldJpaEntity;
import org.datamate.authz.domain.model.policy.entity.ConditionField;
import org.datamate.authz.domain.model.policy.enumtype.FieldType;
import org.datamate.authz.domain.model.policy.enumtype.FieldStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ConditionFieldPersistenceMapper {

    private final JsonMapper jsonMapper;

    public ConditionField toDomain(ConditionFieldJpaEntity e) {
        if (e == null) return null;
        return new ConditionField(
                e.getId(), e.getPermissionId(), e.getFieldName(), e.getFieldType(),
                e.getDisplayName(), jsonMapper.deserializeList(e.getAllowedValues()),
                e.getOptionsEndpoint(), e.getStatus(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getDeletedAt()
        );
    }

    public void updateEntity(ConditionFieldJpaEntity entity, Long id, Long permissionId, String fieldName,
                             FieldType fieldType, String displayName, List<String> allowedValues, String optionsEndpoint) {
        if (entity.getId() == null) {
            entity.setId(id);
        }
        entity.setPermissionId(permissionId);
        entity.setFieldName(fieldName);
        entity.setFieldType(fieldType);
        entity.setDisplayName(displayName);
        entity.setAllowedValues(jsonMapper.serializeList(allowedValues));
        entity.setOptionsEndpoint(optionsEndpoint);
        entity.setStatus(FieldStatus.ACTIVE);
        entity.setDeletedAt(null);
    }
}
