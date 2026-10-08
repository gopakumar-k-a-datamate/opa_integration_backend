package org.datamate.collaboration.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;
import io.jsonwebtoken.JwsHeader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Key;

/**
 * Service responsible for verifying and parsing JWT Chat Tickets.
 */
@Component
@RequiredArgsConstructor
public class JwtVerifier {

    private final KeyRegistry keyRegistry;

    /**
     * Verifies the given token string and extracts its claims.
     *
     * @param token the raw JWT token.
     * @return the verified Claims.
     * @throws JwtException if the token is invalid, expired, or improperly signed.
     */
    public Claims verify(String token) throws JwtException {
        // jjwt 0.12+ KeyLocator dynamically resolves the key based on the header's 'kid'
        Locator<Key> keyLocator = header -> {
            if (!(header instanceof JwsHeader jwsHeader)) {
                throw new JwtException("Expected a JWS Header");
            }
            
            String kid = jwsHeader.getKeyId();
            if (kid == null || kid.isBlank()) {
                throw new JwtException("JWT Header is missing 'kid' claim");
            }

            return keyRegistry.getKey(kid)
                    .orElseThrow(() -> new JwtException("Unknown kid: " + kid));
        };

        return Jwts.parser()
                .keyLocator(keyLocator)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
