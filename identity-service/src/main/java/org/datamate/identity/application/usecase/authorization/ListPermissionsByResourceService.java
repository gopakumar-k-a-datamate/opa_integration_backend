package org.datamate.identity.application.usecase.authorization;

import java.util.List;
import java.util.UUID;
import org.datamate.identity.application.dto.authorization.PermissionDto;
import org.datamate.identity.application.port.in.authorization.ListPermissionsByResourceUseCase;
import org.datamate.identity.application.port.out.authorization.PermissionPersistencePort;
import org.springframework.stereotype.Service;

@Service
public class ListPermissionsByResourceService implements ListPermissionsByResourceUseCase {

    private final PermissionPersistencePort persistencePort;

    public ListPermissionsByResourceService(PermissionPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }


    @Override
    public List<PermissionDto> listPermissions(UUID resourceId) {
        return persistencePort.findByResourceId(resourceId).stream()
                .map(p -> new PermissionDto(p.getId(), p.getResourceId(), p.getAction(), p.getCode(), p.getDescription()))
                .toList();
    }
}
