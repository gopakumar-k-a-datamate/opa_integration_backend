package org.datamate.identity.auth.application.port.in.auth;

import org.datamate.identity.auth.application.dto.auth.AuthResponse;
import org.datamate.identity.auth.application.dto.auth.RefreshTokenRequest;

public interface RefreshTokenUseCase {
    AuthResponse refreshToken(RefreshTokenRequest request);
}
