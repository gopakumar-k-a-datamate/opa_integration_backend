package org.datamate.identity.application.port.in.role;

import org.datamate.identity.application.dto.role.RoleDto;
import org.datamate.identity.application.query.role.RoleSearchCriteria;
import java.util.List;

public interface ListRolesUseCase {
    List<RoleDto> listRoles(RoleSearchCriteria criteria);
}
