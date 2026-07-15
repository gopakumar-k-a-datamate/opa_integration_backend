package org.datamate.identity.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Role {
    private final UUID id;
    private final String name;
    private final String description;

    public static Role create(String name, String description) {
        return new Role(UUID.randomUUID(), name, description);
    }

    public static Role reconstitute(UUID id, String name, String description) {
        return new Role(id, name, description);
    }
}
