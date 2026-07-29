package org.datamate.identity.adapter.out.persistence.mapper;

import org.datamate.identity.adapter.out.persistence.entity.RoleJpaEntity;
import org.datamate.identity.adapter.out.persistence.entity.UserJpaEntity;
import org.datamate.identity.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserPersistenceMapper {

    public User mapToDomain(UserJpaEntity entity) {
        if (entity == null) return null;
        return User.reconstitute(
                entity.getId(),
                entity.getUserName(),
                entity.getEmail(),
                entity.getPhoneNumber(),
                entity.getPasswordHash(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getReferenceSystem(),
                entity.getReferenceValue(),
                entity.getStatus(),
                entity.getRoles() != null 
                        ? entity.getRoles().stream().map(RoleJpaEntity::getName).collect(Collectors.toList()) 
                        : java.util.Collections.emptyList(),
                entity.getVersion(),
                entity.getDomainVersion(),
                entity.getCreatedBy(),
                entity.getCreatedDate(),
                entity.getLastModifiedBy(),
                entity.getLastModifiedDate()
        );
    }

    public UserJpaEntity mapToJpaEntity(User user) {
        if (user == null) return null;
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setUserName(user.getUserName());
        entity.setEmail(user.getEmail());
        entity.setPhoneNumber(user.getPhoneNumber());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setReferenceSystem(user.getReferenceSystem());
        entity.setReferenceValue(user.getReferenceValue());
        entity.setStatus(user.getStatus());
        entity.setVersion(user.getVersion());
        entity.setDomainVersion(user.getDomainVersion());
        entity.setCreatedBy(user.getCreatedBy());
        entity.setCreatedDate(user.getCreatedDate());
        entity.setLastModifiedBy(user.getLastModifiedBy());
        entity.setLastModifiedDate(user.getLastModifiedDate());
        return entity;
    }
}
