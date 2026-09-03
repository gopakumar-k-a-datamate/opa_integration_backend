package org.datamate.authz.jpa.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity representing a locally-replicated subject (USER or ROLE) synced
 * from the Identity Service via the {@code auth.subject.sync} RabbitMQ exchange.
 *
 * <h2>Unified Subject Model</h2>
 * <p>Both USER and ROLE subjects share this single table ({@code authz_subject}).
 * Fields that are not applicable to a given subject type are stored as {@code null}:</p>
 * <ul>
 *   <li>{@code email} — populated for USER, {@code null} for ROLE</li>
 *   <li>{@code description} — populated for ROLE, {@code null} for USER</li>
 * </ul>
 *
 * <h2>Soft-Delete Pattern</h2>
 * <p>Active subjects have {@code deletedAt = null}.
 * Deactivated subjects have {@code deletedAt} set to the timestamp of deactivation.
 * The {@code status} field mirrors the Identity Service's status string for richer querying.</p>
 */
@Entity
@Table(name = "authz_subject")
public class AuthzSubjectJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** {@code "USER"} or {@code "ROLE"} — extensible for future subject types. */
    @Column(name = "subject_type", nullable = false)
    private String subjectType;

    /** IdP-issued UUID identifier for the subject. */
    @Column(name = "subject_id", nullable = false)
    private String subjectId;

    /** Internal identifier: userName for users, role code/name for roles. */
    @Column(name = "subject_name", nullable = false)
    private String subjectName;

    /** Human-readable label: "John Doe" for users, role name for roles. */
    @Column(name = "display_name")
    private String displayName;

    /** User email address. {@code null} for ROLE subjects. */
    @Column(name = "email")
    private String email;

    /** Role description. {@code null} for USER subjects. */
    @Column(name = "description")
    private String description;

    /** Mirrors Identity Service status: {@code "ACTIVE"} or {@code "INACTIVE"}. */
    @Column(name = "status", nullable = false)
    private String status = "INACTIVE";

    /**
     * Monotonically increasing domain version from the Identity Service.
     * Used to guard against out-of-order event delivery.
     */
    @Column(nullable = false)
    private Long version = 0L;

    /** When this row was first created locally. Set once on insert, never updated. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** When this row was last updated locally. Auto-managed by Hibernate. */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** When the latest Identity Service sync event was applied to this row. */
    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    /**
     * {@code null} = subject is active.
     * Non-null = subject was deactivated at this timestamp (soft-delete).
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public AuthzSubjectJpaEntity() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubjectType() { return subjectType; }
    public void setSubjectType(String subjectType) { this.subjectType = subjectType; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
