package org.datamate.identity.adapter.out.persistence.mapper;

import org.datamate.identity.adapter.out.persistence.entity.RoleJpaEntity;
import org.datamate.identity.domain.model.Role;
import org.springframework.stereotype.Component;

@Component
public class RolePersistenceMapper {
    public Role mapToDomain(RoleJpaEntity entity) {
        if (entity == null) return null;
        return Role.reconstitute(entity.getId(), entity.getName(), entity.getDescription());
    }

    public RoleJpaEntity mapToJpaEntity(Role role) {
        if (role == null) return null;
        RoleJpaEntity entity = new RoleJpaEntity();
        entity.setId(role.getId());
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());
        return entity;
    }
}
