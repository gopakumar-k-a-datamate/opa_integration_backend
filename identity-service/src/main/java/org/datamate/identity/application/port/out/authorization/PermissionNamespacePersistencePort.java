package org.datamate.identity.application.port.out.authorization;

import java.util.List;
import org.datamate.identity.domain.model.authorization.entity.PermissionNamespace;

public interface PermissionNamespacePersistencePort {
    List<PermissionNamespace> findAll();
}
