package org.datamate.identity.application.port.in.role;

import org.datamate.identity.application.dto.role.RoleSelectDto;
import java.util.List;

public interface SelectRolesUseCase {
    List<RoleSelectDto> selectRoles(String search);
}
