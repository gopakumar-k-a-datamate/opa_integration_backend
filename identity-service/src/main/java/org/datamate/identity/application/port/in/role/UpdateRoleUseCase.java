package org.datamate.identity.application.port.in.role;

import org.datamate.identity.application.dto.role.RoleDto;
import org.datamate.identity.application.dto.role.UpdateRoleRequest;
import com.datamate.bedrock.framework.common.ddd.datatype.EntityReference;

import java.util.UUID;

public interface UpdateRoleUseCase {
    RoleDto updateRole(UUID id, UpdateRoleRequest request, EntityReference<UUID> adminUserRef);
}
