package org.datamate.identity.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Role {
    private final Long id;
    private final String name;
    private final String description;

    public static Role create(String name, String description) {
        return new Role(null, name, description);
    }

    public static Role reconstitute(Long id, String name, String description) {
        return new Role(id, name, description);
    }
}
