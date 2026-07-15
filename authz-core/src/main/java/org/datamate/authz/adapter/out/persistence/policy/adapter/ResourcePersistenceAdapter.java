package org.datamate.authz.adapter.out.persistence.policy.adapter;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.adapter.out.persistence.policy.entity.ResourceJpaEntity;
import org.datamate.authz.adapter.out.persistence.policy.repository.SpringDataResourceRepository;
import org.datamate.authz.application.port.out.policy.ResourcePersistencePort;
import org.datamate.authz.domain.model.policy.entity.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class ResourcePersistenceAdapter implements ResourcePersistencePort {

    private final SpringDataResourceRepository repository;
@Override
    public Resource upsert(UUID id, String namespace, String name, String description) {
        ResourceJpaEntity entity = repository
                .findByNamespaceAndNameAndDeletedAtIsNull(namespace, name)
                .orElseGet(ResourceJpaEntity::new);

        if (entity.getId() == null) {
            entity.setId(id);
        }
        entity.setNamespace(namespace);
        entity.setName(name);
        entity.setDescription(description);
        entity.setDeletedAt(null);

        return toDomain(repository.save(entity));
    }

    @Override
    public List<Resource> findAllActive() {
        return repository.findAllByDeletedAtIsNull().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Resource> findByNamespaceAndName(String namespace, String name) {
        return repository.findByNamespaceAndNameAndDeletedAtIsNull(namespace, name)
                .map(this::toDomain);
    }

    private Resource toDomain(ResourceJpaEntity e) {
        return new Resource(e.getId(), e.getNamespace(), e.getName(),
                e.getDescription(), e.getCreatedAt(), e.getUpdatedAt(), e.getDeletedAt());
    }
}



