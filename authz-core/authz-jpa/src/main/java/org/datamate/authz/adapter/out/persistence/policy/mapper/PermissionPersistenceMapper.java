package org.datamate.authz.adapter.out.persistence.policy.mapper;

import org.datamate.authz.adapter.out.persistence.policy.entity.PermissionJpaEntity;
import org.datamate.authz.domain.model.policy.entity.Permission;
import org.springframework.stereotype.Component;


@Component
public class PermissionPersistenceMapper {
    public Permission toDomain(PermissionJpaEntity e) {
        if (e == null) return null;
        return Permission.reconstitute(e.getId(), e.getResourceId(), e.getAction(), e.getCode(),
                e.getDescription(), e.getCreatedAt(), e.getUpdatedAt(), e.getDeletedAt());
    }

    public void updateEntity(PermissionJpaEntity entity, Long id, Long resourceId, String action, String code, String description) {
        if (entity.getId() == null) {
            entity.setId(id);
        }
        entity.setResourceId(resourceId);
        entity.setAction(action);
        entity.setCode(code);
        entity.setDescription(description);
        entity.setDeletedAt(null);
    }
}
