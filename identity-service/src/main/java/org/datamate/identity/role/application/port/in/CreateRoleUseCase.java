package org.datamate.identity.role.application.port.in;

import org.datamate.identity.role.application.dto.RoleDto;
import org.datamate.identity.role.application.dto.RoleRequest;

public interface CreateRoleUseCase {
    RoleDto createRole(RoleRequest request);
}


