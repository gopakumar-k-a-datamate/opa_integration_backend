package org.datamate.identity.adapter.out.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.adapter.out.persistence.entity.UserJpaEntity;
import org.datamate.identity.adapter.out.persistence.mapper.UserPersistenceMapper;
import org.datamate.identity.adapter.out.persistence.repository.SpringDataUserRepository;
import org.datamate.identity.application.port.out.UserPersistencePort;
import org.datamate.identity.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {
    private final SpringDataUserRepository repository;
    private final UserPersistenceMapper mapper;

    @Override
    public void save(User user) {
        UserJpaEntity entity = mapper.mapToJpaEntity(user);
        repository.save(entity);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::mapToDomain);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream().map(mapper::mapToDomain).toList();
    }
}
