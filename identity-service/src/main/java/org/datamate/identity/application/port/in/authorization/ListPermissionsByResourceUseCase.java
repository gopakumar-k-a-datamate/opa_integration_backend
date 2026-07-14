package org.datamate.identity.application.port.in.authorization;

import java.util.List;
import java.util.UUID;
import org.datamate.identity.application.dto.authorization.PermissionDto;

public interface ListPermissionsByResourceUseCase {
    List<PermissionDto> listPermissions(UUID resourceId);
}
