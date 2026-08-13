package org.datamate.authz.jpa.entity;

import jakarta.persistence.*;
import org.datamate.authz.model.policy.enumtype.FieldType;
import org.datamate.authz.model.policy.enumtype.FieldStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "authz_condition_field",
        uniqueConstraints = @UniqueConstraint(columnNames = {"permission_id", "field_name"}))
public class ConditionFieldJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission_id", nullable = false)
    private Long permissionId;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false)
    private FieldType fieldType;

    @Column(name = "display_name")
    private String displayName;

    /** JSON array string, e.g. {@code ["CASH","HDFC","SBI"]}. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_values", columnDefinition = "jsonb")
    private String allowedValues;

    @Column(name = "options_endpoint")
    private String optionsEndpoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FieldStatus status = FieldStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public ConditionFieldJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAllowedValues() {
        return allowedValues;
    }

    public String getOptionsEndpoint() {
        return optionsEndpoint;
    }

    public FieldStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setAllowedValues(String allowedValues) {
        this.allowedValues = allowedValues;
    }

    public void setOptionsEndpoint(String optionsEndpoint) {
        this.optionsEndpoint = optionsEndpoint;
    }

    public void setStatus(FieldStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

}
