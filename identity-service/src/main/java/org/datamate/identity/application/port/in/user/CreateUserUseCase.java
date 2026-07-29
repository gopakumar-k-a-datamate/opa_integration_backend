package org.datamate.identity.application.port.in.user;

import org.datamate.identity.application.command.user.CreateUserCommand;
import org.datamate.identity.application.dto.user.UserDto;

public interface CreateUserUseCase {
    UserDto createUser(CreateUserCommand command);
}
