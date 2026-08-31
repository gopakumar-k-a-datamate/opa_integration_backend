package org.datamate.identity.auth.domain.exception;

public class InvalidRefreshTokenException extends DomainException {
    public InvalidRefreshTokenException() {
        super("auth.invalidRefreshToken", "Invalid or expired refresh token.");
    }
}
