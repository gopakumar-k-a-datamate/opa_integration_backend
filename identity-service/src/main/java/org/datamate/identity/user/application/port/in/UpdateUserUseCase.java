package org.datamate.identity.user.application.port.in;

import org.datamate.identity.user.application.dto.UpdateUserRequest;
import org.datamate.identity.user.application.dto.UserDto;

import java.util.UUID;

public interface UpdateUserUseCase {
    UserDto updateUser(UUID id, UpdateUserRequest request, String adminUsername);
}


