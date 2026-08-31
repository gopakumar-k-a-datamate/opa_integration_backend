package org.datamate.identity.user.application.port.in.user;

import org.datamate.identity.user.application.dto.user.ChangePasswordRequest;
import org.datamate.identity.user.application.dto.user.UserDto;
import java.util.UUID;

public interface ChangePasswordUseCase {
    UserDto changePassword(UUID userId, ChangePasswordRequest request);
}
