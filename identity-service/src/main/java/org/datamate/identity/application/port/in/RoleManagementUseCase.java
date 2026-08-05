package org.datamate.identity.application.port.in;

import org.datamate.identity.application.dto.RoleDto;
import org.datamate.identity.application.dto.RoleRequest;
import java.util.List;

public interface RoleManagementUseCase {
    RoleDto createRole(RoleRequest request);
    RoleDto getRole(Long id);
    List<RoleDto> listRoles();
    void deleteRole(Long id);
}
