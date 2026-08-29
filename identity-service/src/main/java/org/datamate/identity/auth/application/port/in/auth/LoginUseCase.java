package org.datamate.identity.auth.application.port.in.auth;

import org.datamate.identity.auth.application.dto.auth.AuthResponse;
import org.datamate.identity.auth.application.dto.auth.LoginRequest;

public interface LoginUseCase {
    AuthResponse login(LoginRequest request);
    void logout(String token);
}
