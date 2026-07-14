package org.datamate.identity.adapter.out.persistence.authorization.adapter;

import java.util.List;
import java.util.UUID;
import org.datamate.identity.adapter.out.persistence.authorization.repository.SpringDataPermissionRepository;
import org.datamate.identity.application.port.out.authorization.PermissionPersistencePort;
import org.datamate.identity.domain.model.authorization.entity.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionPersistenceAdapter implements PermissionPersistencePort {

    private final SpringDataPermissionRepository repository;

    public PermissionPersistenceAdapter(SpringDataPermissionRepository repository) {
        this.repository = repository;
    }


    @Override
    public List<Permission> findByResourceId(UUID resourceId) {
        return repository.findByResourceId(resourceId).stream()
                .map(entity -> new Permission(entity.getId(), entity.getResourceId(), entity.getAction(), entity.getCode(), entity.getDescription()))
                .toList();
    }
}
