package org.datamate.identity.adapter.out.persistence.role.mapper;

import org.datamate.identity.adapter.out.persistence.role.entity.RoleJpaEntity;
import org.datamate.identity.domain.model.Role;
import org.springframework.stereotype.Component;

@Component
public class RolePersistenceMapper {
    public Role mapToDomain(RoleJpaEntity entity) {
        if (entity == null) return null;
        return Role.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getReferenceSystem(),
                entity.getReferenceValue(),
                entity.getVersion(),
                entity.getDomainVersion(),
                entity.getCreatedBy(),
                entity.getCreatedDate(),
                entity.getLastModifiedBy(),
                entity.getLastModifiedDate()
        );
    }

    public RoleJpaEntity mapToJpaEntity(Role role) {
        if (role == null) return null;
        RoleJpaEntity entity = new RoleJpaEntity();
        entity.setId(role.getId());
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());
        entity.setStatus(role.getStatus());
        entity.setReferenceSystem(role.getReferenceSystem());
        entity.setReferenceValue(role.getReferenceValue());
        entity.setVersion(role.getVersion());
        entity.setDomainVersion(role.getDomainVersion());
        entity.setCreatedBy(role.getCreatedBy());
        entity.setCreatedDate(role.getCreatedDate());
        entity.setLastModifiedBy(role.getLastModifiedBy());
        entity.setLastModifiedDate(role.getLastModifiedDate());
        return entity;
    }
}
