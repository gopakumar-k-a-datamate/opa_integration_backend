package org.datamate.identity.domain.model.authorization.entity;

import java.util.UUID;
import lombok.Getter;

/**
 * Represents a high-level grouping or tenant for authorization purposes.
 * PermissionNamespaces ensure that resources and permissions are isolated
 * within specific bounded contexts, preventing overlap across different domains or customers.
 */
@Getter
public class PermissionNamespace {
    private final UUID id;
    private final String name;
    private final String description;

    public PermissionNamespace(UUID id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
