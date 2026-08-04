package org.datamate.authz.jpa.service;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.jpa.entity.PermissionJpaEntity;
import org.datamate.authz.jpa.repository.SpringDataPermissionRepository;
import org.datamate.authz.api.policy.PermissionRepository;
import org.datamate.authz.model.policy.entity.Permission;
import org.datamate.authz.jpa.mapper.PermissionPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JpaPermissionRepository implements PermissionRepository {

    private final SpringDataPermissionRepository repository;
    private final PermissionPersistenceMapper mapper;

    @Override
    public Permission upsert(Long id, Long resourceId, String action, String code,
                                  String description) {
        PermissionJpaEntity entity = repository
                .findByResourceIdAndActionAndDeletedAtIsNull(resourceId, action)
                .orElseGet(PermissionJpaEntity::new);

        mapper.updateEntity(entity, id, resourceId, action, code, description);

        return mapper.toDomain(repository.save(entity));
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



