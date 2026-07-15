package org.datamate.identity.application.port.in;

import org.datamate.identity.application.dto.RoleDto;
import org.datamate.identity.application.dto.RoleRequest;
import java.util.List;
import java.util.UUID;

public interface RoleManagementUseCase {
    RoleDto createRole(RoleRequest request);
    RoleDto getRole(UUID id);
    List<RoleDto> listRoles();
    void deleteRole(UUID id);
}
