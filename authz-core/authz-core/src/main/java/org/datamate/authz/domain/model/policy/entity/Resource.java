package org.datamate.authz.domain.model.policy.entity;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Represents a protected entity within an application module.
 * Auto-registered from {@code @PolicyResource} annotations on startup.
 *
 * <p>Example: {@code namespace="finance", name="journal"}</p>
 */
@Getter
public class Resource {
    private final Long id;
    private final String namespace;
    private final String name;
    private final String description;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    private Resource(Long id, String namespace, String name, String description,
                         LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.namespace = namespace;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static Resource create(String namespace, String name, String description) {
        return new Resource(null, namespace, name, description, LocalDateTime.now(), null, null);
    }

    public static Resource reconstitute(Long id, String namespace, String name, String description,
                                        LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        return new Resource(id, namespace, name, description, createdAt, updatedAt, deletedAt);
    }

    public boolean isActive() {
        return deletedAt == null;
    }
}


