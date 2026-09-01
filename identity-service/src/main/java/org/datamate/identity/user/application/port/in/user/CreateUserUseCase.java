package org.datamate.identity.user.application.port.in.user;

import org.datamate.identity.user.application.dto.user.CreateUserRequest;
import org.datamate.identity.user.application.dto.user.UserDto;

public interface CreateUserUseCase {
    UserDto createUser(CreateUserRequest request);
}
