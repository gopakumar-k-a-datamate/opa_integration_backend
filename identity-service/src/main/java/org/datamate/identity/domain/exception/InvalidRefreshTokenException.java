package org.datamate.identity.domain.exception;

public class InvalidRefreshTokenException extends DomainException {
    public InvalidRefreshTokenException() {
        super("auth.invalidRefreshToken", "Invalid or expired refresh token.");
    }
}
