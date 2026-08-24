package org.datamate.identity.user.adapter.out.persistence.user.mapper;

import org.datamate.identity.role.adapter.out.persistence.role.entity.RoleJpaEntity;
import org.datamate.identity.role.adapter.out.persistence.role.repository.SpringDataRoleRepository;
import org.datamate.identity.user.adapter.out.persistence.user.entity.UserJpaEntity;
import org.datamate.identity.user.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.datamate.identity.role.domain.exception.RoleNotFoundException;

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

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<RoleJpaEntity> roleEntitiesList = roleRepository.findAllByNameIn(user.getRoles());
            if (roleEntitiesList.size() != user.getRoles().size()) {
                throw new RoleNotFoundException();
            }
            entity.setRoles(new HashSet<>(roleEntitiesList));
        } else {
            entity.getRoles().clear();
        }
    }
}


