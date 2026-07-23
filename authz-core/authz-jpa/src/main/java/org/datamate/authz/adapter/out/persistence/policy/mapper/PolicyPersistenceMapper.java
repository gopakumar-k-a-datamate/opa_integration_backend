package org.datamate.authz.adapter.out.persistence.policy.mapper;

import org.datamate.authz.adapter.out.persistence.policy.entity.PolicyJpaEntity;
import org.datamate.authz.domain.model.policy.entity.Policy;
import org.datamate.authz.domain.model.policy.enumtype.PolicyEffect;
import org.datamate.authz.domain.model.policy.enumtype.SubjectType;
import org.springframework.stereotype.Component;


@Component
public class PolicyPersistenceMapper {
    public Policy toDomain(PolicyJpaEntity e) {
        if (e == null) return null;
        return Policy.reconstitute(
                e.getId(), e.getPermissionId(), e.getSubjectType(), e.getSubjectId(),
                e.getEffect(), e.getExpressionJson(), e.isEnabled(), e.getDisabledReason(),
                e.isDeprecated(), e.getVersion(), e.getCreatedAt(), e.getUpdatedAt(), e.getDeletedAt(), e.getDeletedReason()
        );
    }

    public void updateEntity(PolicyJpaEntity entity, Long id, Long permissionId, SubjectType subjectType, String subjectId,
                             PolicyEffect effect, String expressionJson, boolean enabled, String disabledReason) {
        if (entity.getId() == null) {
            entity.setId(id);
        }
        entity.setPermissionId(permissionId);
        entity.setSubjectType(subjectType);
        entity.setSubjectId(subjectId);
        entity.setEffect(effect);
        entity.setExpressionJson(expressionJson);
        entity.setEnabled(enabled);
        entity.setDisabledReason(disabledReason);
        entity.setDeletedAt(null);
    }
}
