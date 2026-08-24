package org.datamate.identity.role.application.port.in;

import org.datamate.identity.role.application.dto.RoleSelectDto;
import java.util.List;

public interface SelectRolesUseCase {
    List<RoleSelectDto> selectRoles(String search);
}


