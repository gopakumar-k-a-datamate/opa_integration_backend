package org.datamate.authz.adapter.out.persistence.policy.adapter;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.adapter.out.persistence.policy.entity.PermissionJpaEntity;
import org.datamate.authz.adapter.out.persistence.policy.repository.SpringDataPermissionRepository;
import org.datamate.authz.application.port.out.policy.PermissionPersistencePort;
import org.datamate.authz.domain.model.policy.entity.Permission;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class PermissionPersistenceAdapter implements PermissionPersistencePort {

    private final SpringDataPermissionRepository repository;
@Override
    public Permission upsert(UUID id, UUID resourceId, String action, String code,
                                  String description) {
        PermissionJpaEntity entity = repository
                .findByResourceIdAndActionAndDeletedAtIsNull(resourceId, action)
                .orElseGet(PermissionJpaEntity::new);

        if (entity.getId() == null) {
            entity.setId(id);
        }
        entity.setResourceId(resourceId);
        entity.setAction(action);
        entity.setCode(code);
        entity.setDescription(description);
        entity.setDeletedAt(null);

        return toDomain(repository.save(entity));
    }

    @Override
    public List<Permission> findByResourceId(UUID resourceId) {
        return repository.findByResourceIdAndDeletedAtIsNull(resourceId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Permission> findByCode(String code) {
        return repository.findByCodeAndDeletedAtIsNull(code).map(this::toDomain);
    }

    @Override
    public List<Permission> findAllActive() {
        return repository.findAllByDeletedAtIsNull().stream().map(this::toDomain).toList();
    }

    private Permission toDomain(PermissionJpaEntity e) {
        return new Permission(e.getId(), e.getResourceId(), e.getAction(), e.getCode(),
                e.getDescription(), e.getCreatedAt(), e.getUpdatedAt(), e.getDeletedAt());
    }
}



