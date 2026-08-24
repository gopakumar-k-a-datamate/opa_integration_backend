package org.datamate.identity.auth.application.port.in;

import org.datamate.identity.auth.application.dto.AuthResponse;
import org.datamate.identity.auth.application.dto.LoginRequest;

public interface LoginUseCase {
    AuthResponse login(LoginRequest request);
    void logout(String token);
}


