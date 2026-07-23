package org.datamate.authz.domain.model.policy.entity;

import lombok.Getter;

import org.datamate.authz.domain.model.policy.enumtype.Status;

import java.time.LocalDateTime;

/**
 * Represents a specific action that can be performed on an {@link Resource}.
 * Auto-registered from {@code @PolicyResource} annotations on startup.
 *
 * <p>The {@code code} is auto-generated as {@code {namespace}:{resource}:{action}},
 * e.g. {@code "finance:journal:create"}.</p>
 */
@Getter
public class Permission {
    private final Long id;
    private final Long resourceId;
    private final String action;

    /** Auto-generated composite code: {namespace}:{resource}:{action}. */
    private final String code;
    private final String description;
    private final Status status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    private Permission(Long id, Long resourceId, String action, String code,
                           String description, Status status, LocalDateTime createdAt,
                           LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.resourceId = resourceId;
        this.action = action;
        this.code = code;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static Permission create(Long resourceId, String action, String namespace, String resourceName, String description) {
        String code = namespace + ":" + resourceName + ":" + action;
        return new Permission(null, resourceId, action, code, description, Status.ACTIVE, LocalDateTime.now(), null, null);
    }

    public static Permission reconstitute(Long id, Long resourceId, String action, String code,
                                          String description, Status status, LocalDateTime createdAt,
                                          LocalDateTime updatedAt, LocalDateTime deletedAt) {
        return new Permission(id, resourceId, action, code, description, status, createdAt, updatedAt, deletedAt);
    }

    public boolean isActive() {
        return deletedAt == null;
    }
}


