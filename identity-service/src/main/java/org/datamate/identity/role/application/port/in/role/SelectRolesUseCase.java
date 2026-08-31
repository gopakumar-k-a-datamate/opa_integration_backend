package org.datamate.identity.role.application.port.in.role;

import org.datamate.identity.role.application.dto.role.RoleSelectDto;
import java.util.List;

public interface SelectRolesUseCase {
    List<RoleSelectDto> selectRoles(String search);
}
