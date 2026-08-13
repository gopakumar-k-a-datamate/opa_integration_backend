package org.datamate.identity.application.port.in.role;

import org.datamate.identity.application.dto.role.RoleDto;
import java.util.UUID;

public interface GetRoleUseCase {
    RoleDto getRoleById(UUID id);
}
