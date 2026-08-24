package org.datamate.identity.role.application.port.in;

import java.util.UUID;

public interface RoleManagementUseCase {
    void deleteRole(UUID id);
}

