package org.datamate.identity.application.port.in.role;

import org.datamate.identity.application.dto.role.RoleDto;
import org.datamate.identity.application.dto.role.RoleRequest;
import java.util.List;
import java.util.UUID;

public interface RoleManagementUseCase {

    RoleDto getRole(UUID id);
    void deleteRole(UUID id);
}
