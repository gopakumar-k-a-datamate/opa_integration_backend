package org.datamate.identity.application.port.in.user;

import org.datamate.identity.application.dto.user.UpdateUserRequest;
import org.datamate.identity.application.dto.user.UserDto;

import java.util.UUID;

public interface UpdateUserUseCase {
    UserDto updateUser(UUID id, UpdateUserRequest request, String adminUsername);
}
