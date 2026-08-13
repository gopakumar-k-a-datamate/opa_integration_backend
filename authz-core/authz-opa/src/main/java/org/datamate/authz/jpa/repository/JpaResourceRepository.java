package org.datamate.authz.jpa.repository;


import org.datamate.authz.jpa.entity.ResourceJpaEntity;
import org.datamate.authz.jpa.repository.ResourceJpaRepository;
import org.datamate.authz.api.policy.ResourceRepository;
import org.datamate.authz.model.policy.entity.Resource;
import org.datamate.authz.model.policy.enumtype.Status;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaResourceRepository implements ResourceRepository {

    public JpaResourceRepository(ResourceJpaRepository repository) {
        this.repository = repository;
    }

    private final ResourceJpaRepository repository;

    @Override
    public Resource upsert(Long id, String namespace, String name, String description) {
        ResourceJpaEntity entity = repository
                .findByNamespaceAndNameAndDeletedAtIsNull(namespace, name)
                .orElseGet(ResourceJpaEntity::new);

        updateEntity(entity, id, namespace, name, description);

        return toDomain(repository.save(entity));
    }

    @Override
    public List<Resource> findAllActive() {
        return repository.findAllByDeletedAtIsNull().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Resource> findByNamespaceAndName(String namespace, String name) {
        return repository.findByNamespaceAndNameAndDeletedAtIsNull(namespace, name).map(this::toDomain);
    }

    private Resource toDomain(ResourceJpaEntity e) {
        if (e == null) return null;
        return Resource.reconstitute(e.getId(), e.getNamespace(), e.getName(),
                e.getDescription(), e.getStatus(), e.getCreatedAt(), e.getUpdatedAt(), e.getDeletedAt());
    }

    private void updateEntity(ResourceJpaEntity entity, Long id, String namespace, String name, String description) {
        if (entity.getId() == null) {
            entity.setId(id);
        }
        entity.setNamespace(namespace);
        entity.setName(name);
        entity.setDescription(description);
        // By default on creation, it should be ACTIVE. 
        if (entity.getStatus() == null) {
            entity.setStatus(Status.ACTIVE);
        }
        entity.setDeletedAt(null);
    }
}
