package org.datamate.identity.application.command.user;

public record CreateUserCommand(
        String userName,
        String email,
        String phoneNumber,
        String firstName,
        String lastName,
        String password,
        String referenceSystem,
        String referenceValue
) {}
