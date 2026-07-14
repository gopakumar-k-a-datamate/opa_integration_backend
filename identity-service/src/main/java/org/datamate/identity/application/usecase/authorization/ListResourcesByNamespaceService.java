package org.datamate.identity.application.usecase.authorization;

import java.util.List;
import java.util.UUID;
import org.datamate.identity.application.dto.authorization.ResourceDto;
import org.datamate.identity.application.port.in.authorization.ListResourcesByNamespaceUseCase;
import org.datamate.identity.application.port.out.authorization.ResourcePersistencePort;
import org.springframework.stereotype.Service;

@Service
public class ListResourcesByNamespaceService implements ListResourcesByNamespaceUseCase {

    private final ResourcePersistencePort persistencePort;

    public ListResourcesByNamespaceService(ResourcePersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }


    @Override
    public List<ResourceDto> listResources(UUID namespaceId) {
        return persistencePort.findByNamespaceId(namespaceId).stream()
                .map(r -> new ResourceDto(r.getId(), r.getNamespaceId(), r.getName(), r.getDescription()))
                .toList();
    }
}
