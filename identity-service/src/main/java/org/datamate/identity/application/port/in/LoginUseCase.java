package org.datamate.identity.application.port.in;

import org.datamate.identity.application.command.LoginCommand;
import org.datamate.identity.application.dto.AuthResponse;

public interface LoginUseCase {
    AuthResponse login(LoginCommand command);
    void logout(String token);
}
