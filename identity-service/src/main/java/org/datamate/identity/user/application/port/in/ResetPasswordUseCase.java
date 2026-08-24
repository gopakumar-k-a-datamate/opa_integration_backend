package org.datamate.identity.user.application.port.in;

import org.datamate.identity.user.application.dto.ResetPasswordRequest;
import org.datamate.identity.user.application.dto.UserDto;
import java.util.UUID;

public interface ResetPasswordUseCase {
    UserDto resetPassword(UUID userId, ResetPasswordRequest request);
}


