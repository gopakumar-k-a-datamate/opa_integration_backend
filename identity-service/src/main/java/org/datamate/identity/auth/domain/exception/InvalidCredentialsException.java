package org.datamate.identity.auth.domain.exception;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("user.invalidCredentials", "Invalid username or password.");
    }
}
