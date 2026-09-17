package org.datamate.authz.model.subject.entity;

import java.time.LocalDateTime;

/**
 * Domain model representing an authorization subject (User or Role) replicated
 * from the Identity Service.
 *
 * <p>This model is an eventually consistent, read-only projection of the global
 * identity state, optimized for local authorization checks without cross-service
 * synchronous calls.</p>
 */
public class AuthzSubject {
    private final Long id;
    private final String subjectType;
    private final String subjectId;
    private final String subjectName;
    private final Long version;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime syncedAt;
    private final LocalDateTime deletedAt;

    private AuthzSubject(Long id, String subjectType, String subjectId, String subjectName,
                         Long version, LocalDateTime createdAt, LocalDateTime updatedAt,
                         LocalDateTime syncedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.subjectType = subjectType;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.syncedAt = syncedAt;
        this.deletedAt = deletedAt;
    }

    public static AuthzSubject reconstitute(Long id, String subjectType, String subjectId, String subjectName,
                                            Long version, LocalDateTime createdAt, LocalDateTime updatedAt,
                                            LocalDateTime syncedAt, LocalDateTime deletedAt) {
        return new AuthzSubject(id, subjectType, subjectId, subjectName, version, createdAt, updatedAt, syncedAt, deletedAt);
    }

    public boolean isActive() {
        return deletedAt == null;
    }

    public Long getId() {
        return id;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getSyncedAt() {
        return syncedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
