package org.datamate.identity.user.application.port.in.user;

import org.datamate.identity.user.application.dto.user.ResetPasswordRequest;
import org.datamate.identity.user.application.dto.user.UserDto;
import java.util.UUID;

public interface ResetPasswordUseCase {
    UserDto resetPassword(UUID userId, ResetPasswordRequest request);
}
