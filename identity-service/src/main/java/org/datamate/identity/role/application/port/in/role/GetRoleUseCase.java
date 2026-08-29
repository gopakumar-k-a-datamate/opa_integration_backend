package org.datamate.identity.role.application.port.in.role;

import org.datamate.identity.role.application.dto.role.RoleDto;
import java.util.UUID;

public interface GetRoleUseCase {
    RoleDto getRoleById(UUID id);
}
