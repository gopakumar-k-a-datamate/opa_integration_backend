package org.datamate.authz.adapter.out.persistence.policy.mapper;

import org.datamate.authz.adapter.out.persistence.policy.entity.ResourceJpaEntity;
import org.datamate.authz.domain.model.policy.entity.Resource;
import org.springframework.stereotype.Component;


@Component
public class ResourcePersistenceMapper {
    public Resource toDomain(ResourceJpaEntity e) {
        if (e == null) return null;
        return Resource.reconstitute(e.getId(), e.getNamespace(), e.getName(),
                e.getDescription(), e.getCreatedAt(), e.getUpdatedAt(), e.getDeletedAt());
    }

    public void updateEntity(ResourceJpaEntity entity, Long id, String namespace, String name, String description) {
        if (entity.getId() == null) {
            entity.setId(id);
        }
        entity.setNamespace(namespace);
        entity.setName(name);
        entity.setDescription(description);
        entity.setDeletedAt(null);
    }
}
