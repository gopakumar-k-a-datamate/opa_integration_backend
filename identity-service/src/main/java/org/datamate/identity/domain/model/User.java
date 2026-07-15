package org.datamate.identity.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;

    public static User create(String email, String passwordHash, String firstName, String lastName) {
        return new User(UUID.randomUUID(), email, passwordHash, firstName, lastName);
    }

    public static User reconstitute(UUID id, String email, String passwordHash, String firstName, String lastName) {
        return new User(id, email, passwordHash, firstName, lastName);
    }
}
