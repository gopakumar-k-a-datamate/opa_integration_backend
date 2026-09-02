package org.datamate.identity.application.port.in.auth;

import org.datamate.identity.application.dto.auth.AuthResponse;
import org.datamate.identity.application.dto.auth.LoginRequest;

public interface LoginUseCase {
    AuthResponse login(LoginRequest request);
    void logout(String token);
}
