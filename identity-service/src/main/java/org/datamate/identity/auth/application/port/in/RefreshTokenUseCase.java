package org.datamate.identity.auth.application.port.in;

import org.datamate.identity.auth.application.dto.AuthResponse;
import org.datamate.identity.auth.application.dto.RefreshTokenRequest;

public interface RefreshTokenUseCase {
    AuthResponse refreshToken(RefreshTokenRequest request);
}


