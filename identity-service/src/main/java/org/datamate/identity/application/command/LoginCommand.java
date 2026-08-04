package org.datamate.identity.application.command;

public record LoginCommand(String userName, String password) {
    public LoginCommand {
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}
