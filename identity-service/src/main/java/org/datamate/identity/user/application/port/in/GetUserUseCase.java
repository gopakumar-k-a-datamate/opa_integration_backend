package org.datamate.identity.user.application.port.in;

import org.datamate.identity.user.application.dto.UserDto;
import java.util.UUID;

public interface GetUserUseCase {
    UserDto getUserById(UUID id);
}


