package org.datamate.identity.role.application.port.in;

import org.datamate.identity.role.application.dto.RoleDto;
import java.util.UUID;

public interface GetRoleUseCase {
    RoleDto getRoleById(UUID id);
}


