package org.datamate.identity.role.application.port.in;

import org.datamate.identity.role.application.dto.RoleDto;
import org.datamate.identity.role.application.dto.UpdateRoleRequest;
import com.datamate.bedrock.framework.common.ddd.datatype.EntityReference;

import java.util.UUID;

public interface UpdateRoleUseCase {
    RoleDto updateRole(UUID id, UpdateRoleRequest request, EntityReference<UUID> adminUserRef);
}


