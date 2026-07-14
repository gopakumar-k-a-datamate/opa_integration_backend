package org.datamate.identity.domain.model.authorization.entity;

import java.util.UUID;
import lombok.Getter;

/**
 * Represents a specific action that can be performed on a Resource.
 * Permissions define "what action" is allowed (e.g., "create", "read", "delete")
 * and serve as the granular unit of authorization for policy enforcement.
 */
@Getter
public class Permission {
    private final UUID id;
    private final UUID resourceId;
    private final String action;
    private final String code; // e.g., finance:journal:create
    private final String description;

    public Permission(UUID id, UUID resourceId, String action, String code, String description) {
        this.id = id;
        this.resourceId = resourceId;
        this.action = action;
        this.code = code;
        this.description = description;
    }
}
