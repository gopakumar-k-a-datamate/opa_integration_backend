package org.datamate.authz.domain.model.policy.entity;

import lombok.Getter;

import org.datamate.authz.domain.model.policy.enumtype.Status;

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
    private final Status status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    private Resource(Long id, String namespace, String name, String description, Status status,
                         LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.namespace = namespace;
        this.name = name;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static Resource create(String namespace, String name, String description) {
        return new Resource(null, namespace, name, description, Status.ACTIVE, LocalDateTime.now(), null, null);
    }

    public static Resource reconstitute(Long id, String namespace, String name, String description, Status status,
                                        LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        return new Resource(id, namespace, name, description, status, createdAt, updatedAt, deletedAt);
    }

    public boolean isActive() {
        return deletedAt == null;
    }
}


