package org.datamate.authz.domain.model.policy.entity;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a protected entity within an application module.
 * Auto-registered from {@code @PolicyResource} annotations on startup.
 *
 * <p>Example: {@code namespace="finance", name="journal"}</p>
 */
@Getter
public class Resource {
    private final UUID id;
    private final String namespace;
    private final String name;
    private final String description;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public Resource(UUID id, String namespace, String name, String description,
                         LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.namespace = namespace;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public boolean isActive() {
        return deletedAt == null;
    }
}


