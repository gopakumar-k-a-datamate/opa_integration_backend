package org.datamate.identity.application.port.in.role;

import org.datamate.identity.application.dto.role.RoleDto;
import org.datamate.identity.application.query.role.RoleSearchCriteria;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;

public interface ListRolesUseCase {
    Paged<RoleDto> listRoles(RoleSearchCriteria criteria, PageQuery pageQuery);
}
