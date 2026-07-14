package org.datamate.identity.domain.model.authorization.entity;

import java.util.UUID;
import lombok.Getter;

/**
 * Represents an entity or object within a namespace that requires access control.
 * Resources define "what" is being secured (e.g., a "document", "dashboard", or "account")
 * under a specific PermissionNamespace.
 */
@Getter
public class Resource {
    private final UUID id;
    private final UUID namespaceId;
    private final String name;
    private final String description;

    public Resource(UUID id, UUID namespaceId, String name, String description) {
        this.id = id;
        this.namespaceId = namespaceId;
        this.name = name;
        this.description = description;
    }
}
