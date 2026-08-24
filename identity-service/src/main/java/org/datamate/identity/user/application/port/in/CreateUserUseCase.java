package org.datamate.identity.user.application.port.in;

import org.datamate.identity.user.application.dto.CreateUserRequest;
import org.datamate.identity.user.application.dto.UserDto;

public interface CreateUserUseCase {
    UserDto createUser(CreateUserRequest request);
}


