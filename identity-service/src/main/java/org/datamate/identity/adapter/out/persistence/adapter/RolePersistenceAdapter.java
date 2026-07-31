package org.datamate.identity.adapter.out.persistence.adapter;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.adapter.out.persistence.entity.RoleJpaEntity;
import org.datamate.identity.adapter.out.persistence.mapper.RolePersistenceMapper;
import org.datamate.identity.adapter.out.persistence.repository.SpringDataRoleRepository;
import org.datamate.identity.application.port.out.RolePersistencePort;
import org.datamate.identity.domain.model.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RolePersistenceAdapter implements RolePersistencePort {

    @EnableLogger
    private Logger log;

    private final SpringDataRoleRepository repository;
    private final RolePersistenceMapper mapper;

    @Override
    public Role save(Role role) {
        RoleJpaEntity entity = mapper.mapToJpaEntity(role);
        RoleJpaEntity saved = repository.save(entity);
        log.debug("Role '{}' persisted with id {}", saved.getName(), saved.getId());
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
        log.debug("Deleting role with id {}", id);
        repository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public List<String> findRoleNamesByUserId(UUID userId) {
        return repository.findRoleNamesByUserId(userId);
    }
}
