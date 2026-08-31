package org.datamate.identity.role.application.port.in.role;

import org.datamate.identity.role.application.dto.role.RoleDto;
import org.datamate.identity.role.application.dto.role.RoleRequest;

public interface CreateRoleUseCase {
    RoleDto createRole(RoleRequest request);
}
