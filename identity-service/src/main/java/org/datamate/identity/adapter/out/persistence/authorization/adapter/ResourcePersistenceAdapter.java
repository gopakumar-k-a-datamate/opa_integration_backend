package org.datamate.identity.adapter.out.persistence.authorization.adapter;

import java.util.List;
import java.util.UUID;
import org.datamate.identity.adapter.out.persistence.authorization.repository.SpringDataResourceRepository;
import org.datamate.identity.application.port.out.authorization.ResourcePersistencePort;
import org.datamate.identity.domain.model.authorization.entity.Resource;
import org.springframework.stereotype.Component;

@Component
public class ResourcePersistenceAdapter implements ResourcePersistencePort {

    private final SpringDataResourceRepository repository;

    public ResourcePersistenceAdapter(SpringDataResourceRepository repository) {
        this.repository = repository;
    }


    @Override
    public List<Resource> findByNamespaceId(UUID namespaceId) {
        return repository.findByNamespaceId(namespaceId).stream()
                .map(entity -> new Resource(entity.getId(), entity.getNamespaceId(), entity.getName(), entity.getDescription()))
                .toList();
    }
}
