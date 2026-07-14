package org.datamate.identity.application.port.out.authorization;

import java.util.List;
import java.util.UUID;
import org.datamate.identity.domain.model.authorization.entity.Permission;

public interface PermissionPersistencePort {
    List<Permission> findByResourceId(UUID resourceId);
}
