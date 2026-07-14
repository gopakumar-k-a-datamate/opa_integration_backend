package org.datamate.identity.application.port.in.authorization;

import java.util.List;
import java.util.UUID;
import org.datamate.identity.application.dto.authorization.ResourceDto;

public interface ListResourcesByNamespaceUseCase {
    List<ResourceDto> listResources(UUID namespaceId);
}
