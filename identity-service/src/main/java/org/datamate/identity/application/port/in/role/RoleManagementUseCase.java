package org.datamate.identity.application.port.in.role;

import org.datamate.identity.application.dto.role.RoleDto;
import org.datamate.identity.application.dto.role.RoleRequest;
import java.util.List;

public interface RoleManagementUseCase {

    RoleDto getRole(Long id);
    List<RoleDto> listRoles();
    void deleteRole(Long id);
}
