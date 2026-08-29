package org.datamate.identity.user.application.port.in.user;

import org.datamate.identity.user.application.dto.user.UserDto;
import org.datamate.identity.user.application.dto.user.UpdateUserRolesRequest;
import java.util.UUID;

public interface UpdateUserRolesUseCase {
    UserDto updateUserRoles(UUID userId, UpdateUserRolesRequest request, String adminUsername);
}
