package org.datamate.identity.application.port.in;

import org.datamate.identity.application.dto.LoginRequest;
import org.datamate.identity.application.dto.AuthResponse;

public interface LoginUseCase {
    AuthResponse login(LoginRequest request);
}
