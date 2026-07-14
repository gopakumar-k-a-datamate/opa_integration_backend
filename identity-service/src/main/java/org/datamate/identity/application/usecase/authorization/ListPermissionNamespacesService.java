package org.datamate.identity.application.usecase.authorization;

import java.util.List;
import org.datamate.identity.application.dto.authorization.PermissionNamespaceDto;
import org.datamate.identity.application.port.in.authorization.ListPermissionNamespacesUseCase;
import org.datamate.identity.application.port.out.authorization.PermissionNamespacePersistencePort;
import org.springframework.stereotype.Service;

@Service
public class ListPermissionNamespacesService implements ListPermissionNamespacesUseCase {

    private final PermissionNamespacePersistencePort persistencePort;

    public ListPermissionNamespacesService(PermissionNamespacePersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }


    @Override
    public List<PermissionNamespaceDto> listNamespaces() {
        return persistencePort.findAll().stream()
                .map(ns -> new PermissionNamespaceDto(ns.getId(), ns.getName(), ns.getDescription()))
                .toList();
    }
}
