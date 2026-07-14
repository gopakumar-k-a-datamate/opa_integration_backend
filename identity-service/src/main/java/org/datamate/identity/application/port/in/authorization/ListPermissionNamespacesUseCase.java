package org.datamate.identity.application.port.in.authorization;

import java.util.List;
import org.datamate.identity.application.dto.authorization.PermissionNamespaceDto;

public interface ListPermissionNamespacesUseCase {
    List<PermissionNamespaceDto> listNamespaces();
}
