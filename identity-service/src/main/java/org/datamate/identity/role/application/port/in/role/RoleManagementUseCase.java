package org.datamate.identity.role.application.port.in.role;

import java.util.UUID;

public interface RoleManagementUseCase {
    void deleteRole(UUID id);
}
