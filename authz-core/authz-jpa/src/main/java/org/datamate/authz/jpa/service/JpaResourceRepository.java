package org.datamate.authz.jpa.service;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.jpa.entity.ResourceJpaEntity;
import org.datamate.authz.jpa.repository.SpringDataResourceRepository;
import org.datamate.authz.api.policy.ResourceRepository;
import org.datamate.authz.model.policy.entity.Resource;
import org.datamate.authz.jpa.mapper.ResourcePersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JpaResourceRepository implements ResourceRepository {

    private final SpringDataResourceRepository repository;
    private final ResourcePersistenceMapper mapper;

    @Override
    public Resource upsert(Long id, String namespace, String name, String description) {
        ResourceJpaEntity entity = repository
                .findByNamespaceAndNameAndDeletedAtIsNull(namespace, name)
                .orElseGet(ResourceJpaEntity::new);

        mapper.updateEntity(entity, id, namespace, name, description);

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public List<Resource> findAllActive() {
        return repository.findAllByDeletedAtIsNull().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Resource> findByNamespaceAndName(String namespace, String name) {
        return repository.findByNamespaceAndNameAndDeletedAtIsNull(namespace, name).map(mapper::toDomain);
    }
}



