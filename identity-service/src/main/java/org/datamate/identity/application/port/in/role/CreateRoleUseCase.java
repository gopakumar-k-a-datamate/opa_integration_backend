package org.datamate.identity.application.port.in.role;

import org.datamate.identity.application.dto.role.RoleDto;
import org.datamate.identity.application.dto.role.RoleRequest;

public interface CreateRoleUseCase {
    RoleDto createRole(RoleRequest request);
}
