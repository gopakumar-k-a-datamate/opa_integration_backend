package org.datamate.authz.adapter.out.persistence.policy.adapter;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.adapter.out.persistence.policy.entity.ResourceJpaEntity;
import org.datamate.authz.adapter.out.persistence.policy.repository.SpringDataResourceRepository;
import org.datamate.authz.application.port.out.policy.ResourcePersistencePort;
import org.datamate.authz.domain.model.policy.entity.Resource;
import org.datamate.authz.adapter.out.persistence.policy.mapper.ResourcePersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ResourcePersistenceAdapter implements ResourcePersistencePort {

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

}



