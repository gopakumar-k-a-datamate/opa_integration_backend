package org.datamate.identity.adapter.out.persistence.authorization.adapter;

import java.util.List;
import org.datamate.identity.adapter.out.persistence.authorization.repository.SpringDataPermissionNamespaceRepository;
import org.datamate.identity.application.port.out.authorization.PermissionNamespacePersistencePort;
import org.datamate.identity.domain.model.authorization.entity.PermissionNamespace;
import org.springframework.stereotype.Component;

@Component
public class PermissionNamespacePersistenceAdapter implements PermissionNamespacePersistencePort {

    private final SpringDataPermissionNamespaceRepository repository;

    public PermissionNamespacePersistenceAdapter(SpringDataPermissionNamespaceRepository repository) {
        this.repository = repository;
    }


    @Override
    public List<PermissionNamespace> findAll() {
        return repository.findAll().stream()
                .map(entity -> new PermissionNamespace(entity.getId(), entity.getName(), entity.getDescription()))
                .toList();
    }
}
