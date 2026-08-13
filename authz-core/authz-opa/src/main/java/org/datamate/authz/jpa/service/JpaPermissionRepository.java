package org.datamate.authz.jpa.service;


import org.datamate.authz.jpa.entity.PermissionJpaEntity;
import org.datamate.authz.jpa.repository.SpringDataPermissionRepository;
import org.datamate.authz.application.port.out.PermissionRepositoryPort;
import org.datamate.authz.model.policy.entity.Permission;
import org.datamate.authz.model.policy.enumtype.Status;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaPermissionRepository implements PermissionRepositoryPort {

    public JpaPermissionRepository(SpringDataPermissionRepository repository) {
        this.repository = repository;
    }

    private final SpringDataPermissionRepository repository;

    @Override
    public Permission upsert(Long id, Long resourceId, String action, String code,
                                  String description) {
        PermissionJpaEntity entity = repository
                .findByResourceIdAndActionAndDeletedAtIsNull(resourceId, action)
                .orElseGet(PermissionJpaEntity::new);

        updateEntity(entity, id, resourceId, action, code, description);

        return toDomain(repository.save(entity));
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
        if (e == null) return null;
        return Permission.reconstitute(e.getId(), e.getResourceId(), e.getAction(), e.getCode(),
                e.getDescription(), e.getStatus(), e.getCreatedAt(), e.getUpdatedAt(), e.getDeletedAt());
    }

    private void updateEntity(PermissionJpaEntity entity, Long id, Long resourceId, String action, String code, String description) {
        if (entity.getId() == null) {
            entity.setId(id);
        }
        entity.setResourceId(resourceId);
        entity.setAction(action);
        entity.setCode(code);
        entity.setDescription(description);
        if (entity.getStatus() == null) {
            entity.setStatus(Status.ACTIVE);
        }
        entity.setDeletedAt(null);
    }

}
