package org.datamate.identity.role.application.port.in;

import org.datamate.identity.role.application.dto.RoleDto;
import org.datamate.identity.role.application.query.RoleSearchCriteria;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;

public interface ListRolesUseCase {
    Paged<RoleDto> listRoles(RoleSearchCriteria criteria, PageQuery pageQuery);
}


