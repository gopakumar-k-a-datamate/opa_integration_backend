package org.datamate.authz.adapter.out.persistence.policy.adapter;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.adapter.out.persistence.policy.entity.PermissionJpaEntity;
import org.datamate.authz.adapter.out.persistence.policy.repository.SpringDataPermissionRepository;
import org.datamate.authz.application.port.out.policy.PermissionPersistencePort;
import org.datamate.authz.domain.model.policy.entity.Permission;
import org.datamate.authz.adapter.out.persistence.policy.mapper.PermissionPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class PermissionPersistenceAdapter implements PermissionPersistencePort {

    private final SpringDataPermissionRepository repository;
    private final PermissionPersistenceMapper mapper;
@Override
    public Permission upsert(UUID id, UUID resourceId, String action, String code,
                                  String description) {
        PermissionJpaEntity entity = repository
                .findByResourceIdAndActionAndDeletedAtIsNull(resourceId, action)
                .orElseGet(PermissionJpaEntity::new);

        mapper.updateEntity(entity, id, resourceId, action, code, description);

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public List<Permission> findByResourceId(UUID resourceId) {
        return repository.findByResourceIdAndDeletedAtIsNull(resourceId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Permission> findByCode(String code) {
        return repository.findByCodeAndDeletedAtIsNull(code).map(mapper::toDomain);
    }

    @Override
    public List<Permission> findAllActive() {
        return repository.findAllByDeletedAtIsNull().stream().map(mapper::toDomain).toList();
    }

}



