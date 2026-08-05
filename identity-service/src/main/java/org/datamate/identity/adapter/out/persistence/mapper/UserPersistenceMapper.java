package org.datamate.identity.adapter.out.persistence.mapper;

import org.datamate.identity.adapter.out.persistence.entity.UserJpaEntity;
import org.datamate.identity.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {
    public User mapToDomain(UserJpaEntity entity) {
        if (entity == null) return null;
        return User.reconstitute(
                entity.getId(),
                entity.getUserName(),
                entity.getPasswordHash(),
                entity.getFirstName(),
                entity.getLastName()
        );
    }

    public UserJpaEntity mapToJpaEntity(User user) {
        if (user == null) return null;
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setUserName(user.getUserName());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        return entity;
    }
}
