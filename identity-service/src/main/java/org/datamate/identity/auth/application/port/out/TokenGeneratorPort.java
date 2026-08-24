package org.datamate.identity.auth.application.port.out;

import org.datamate.identity.user.domain.model.User;

public interface TokenGeneratorPort {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    void invalidateToken(String token);
    boolean isBlacklisted(String token);
    boolean validateToken(String token);
    String getUsernameFromToken(String token);
    String getTokenType(String token);
}


