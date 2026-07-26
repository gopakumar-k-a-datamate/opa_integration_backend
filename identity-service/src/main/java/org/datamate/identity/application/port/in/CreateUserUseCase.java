package org.datamate.identity.application.port.in;

import org.datamate.identity.application.command.CreateUserCommand;
import org.datamate.identity.application.dto.UserDto;

public interface CreateUserUseCase {
    UserDto createUser(CreateUserCommand command);
}
