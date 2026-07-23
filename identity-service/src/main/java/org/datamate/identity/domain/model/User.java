package org.datamate.identity.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    private final Long id;
    private final String userName;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;

    public static User create(String userName, String passwordHash, String firstName, String lastName) {
        return new User(null, userName, passwordHash, firstName, lastName);
    }

    public static User reconstitute(Long id, String userName, String passwordHash, String firstName, String lastName) {
        return new User(id, userName, passwordHash, firstName, lastName);
    }
}
