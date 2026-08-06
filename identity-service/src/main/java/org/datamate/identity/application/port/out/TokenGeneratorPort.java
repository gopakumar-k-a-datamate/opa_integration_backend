package org.datamate.identity.application.port.out;

import org.datamate.identity.domain.model.User;

public interface TokenGeneratorPort {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    void invalidateToken(String token);
    boolean isBlacklisted(String token);
}
