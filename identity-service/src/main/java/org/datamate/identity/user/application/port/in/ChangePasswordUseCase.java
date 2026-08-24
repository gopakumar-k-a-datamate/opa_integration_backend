package org.datamate.identity.user.application.port.in;

import org.datamate.identity.user.application.dto.ChangePasswordRequest;
import org.datamate.identity.user.application.dto.UserDto;
import java.util.UUID;

public interface ChangePasswordUseCase {
    UserDto changePassword(UUID userId, ChangePasswordRequest request);
}


