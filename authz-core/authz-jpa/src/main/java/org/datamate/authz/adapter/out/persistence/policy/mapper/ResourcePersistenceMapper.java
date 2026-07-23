package org.datamate.authz.adapter.out.persistence.policy.mapper;

import org.datamate.authz.adapter.out.persistence.policy.entity.ResourceJpaEntity;
import org.datamate.authz.domain.model.policy.entity.Resource;
import org.datamate.authz.domain.model.policy.enumtype.Status;
import org.springframework.stereotype.Component;


@Component
public class ResourcePersistenceMapper {
    public Resource toDomain(ResourceJpaEntity e) {
        if (e == null) return null;
        return Resource.reconstitute(e.getId(), e.getNamespace(), e.getName(),
                e.getDescription(), e.getStatus(), e.getCreatedAt(), e.getUpdatedAt(), e.getDeletedAt());
    }

    public void updateEntity(ResourceJpaEntity entity, Long id, String namespace, String name, String description) {
        if (entity.getId() == null) {
            entity.setId(id);
        }
        entity.setNamespace(namespace);
        entity.setName(name);
        entity.setDescription(description);
        // By default on creation, it should be ACTIVE. 
        if (entity.getStatus() == null) {
            entity.setStatus(Status.ACTIVE);
        }
        entity.setDeletedAt(null);
    }
}
