package org.datamate.identity.adapter.out.persistence.user.mapper;

import org.datamate.identity.adapter.out.persistence.role.entity.RoleJpaEntity;
import org.datamate.identity.adapter.out.persistence.role.repository.SpringDataRoleRepository;
import org.datamate.identity.adapter.out.persistence.user.entity.UserJpaEntity;
import org.datamate.identity.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.datamate.identity.domain.exception.role.RoleNotFoundException;

@Component
public class UserPersistenceMapper {

    private final SpringDataRoleRepository roleRepository;

    public UserPersistenceMapper(SpringDataRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

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
                        : Collections.emptyList(),
                entity.isPasswordTemporary(),
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
        entity.setPasswordTemporary(user.isPasswordTemporary());
        entity.setVersion(user.getVersion());
        entity.setDomainVersion(user.getDomainVersion());
        entity.setCreatedBy(user.getCreatedBy());
        entity.setCreatedDate(user.getCreatedDate());
        entity.setLastModifiedBy(user.getLastModifiedBy());
        entity.setLastModifiedDate(user.getLastModifiedDate());
        return entity;
    }

    public void updateJpaEntity(UserJpaEntity entity, User user) {
        if (user == null || entity == null) return;
        entity.setUserName(user.getUserName());
        entity.setEmail(user.getEmail());
        entity.setPhoneNumber(user.getPhoneNumber());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setReferenceSystem(user.getReferenceSystem());
        entity.setReferenceValue(user.getReferenceValue());
        entity.setStatus(user.getStatus());
        entity.setPasswordTemporary(user.isPasswordTemporary());
        entity.setVersion(user.getVersion());
        entity.setDomainVersion(user.getDomainVersion());
        entity.setCreatedBy(user.getCreatedBy());
        entity.setCreatedDate(user.getCreatedDate());
        entity.setLastModifiedBy(user.getLastModifiedBy());
        entity.setLastModifiedDate(user.getLastModifiedDate());

        if (user.getRoles() != null) {
            Set<RoleJpaEntity> roleEntities = user.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(RoleNotFoundException::new))
                    .collect(Collectors.toSet());
            entity.setRoles(roleEntities);
        } else {
            entity.getRoles().clear();
        }
    }
}
