package org.datamate.identity.adapter.out.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.adapter.out.persistence.entity.RoleJpaEntity;
import org.datamate.identity.adapter.out.persistence.mapper.RolePersistenceMapper;
import org.datamate.identity.adapter.out.persistence.repository.SpringDataRoleRepository;
import org.datamate.identity.application.port.out.RolePersistencePort;
import org.datamate.identity.domain.model.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RolePersistenceAdapter implements RolePersistencePort {
    private final SpringDataRoleRepository repository;
    private final RolePersistenceMapper mapper;

    @Override
    public Role save(Role role) {
        RoleJpaEntity entity = mapper.mapToJpaEntity(role);
        RoleJpaEntity saved = repository.save(entity);
        return mapper.mapToDomain(saved);
    }

    @Override
    public Optional<Role> findById(Long id) {
        return repository.findById(id).map(mapper::mapToDomain);
    }

    @Override
    public List<Role> findAll() {
        return repository.findAll().stream().map(mapper::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }
}
