package org.datamate.identity.application.port.out.authorization;

import java.util.List;
import java.util.UUID;
import org.datamate.identity.domain.model.authorization.entity.Resource;

public interface ResourcePersistencePort {
    List<Resource> findByNamespaceId(UUID namespaceId);
}
