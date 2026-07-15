package org.datamate.identity.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;

    public static User create(String email, String passwordHash, String firstName, String lastName) {
        return new User(null, email, passwordHash, firstName, lastName);
    }

    public static User reconstitute(Long id, String email, String passwordHash, String firstName, String lastName) {
        return new User(id, email, passwordHash, firstName, lastName);
    }
}
