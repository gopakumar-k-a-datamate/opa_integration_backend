package org.datamate.authz.jpa.entity;

import jakarta.persistence.*;
import org.datamate.authz.model.policy.enumtype.PolicyEffect;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import org.hibernate.envers.Audited;
import org.hibernate.envers.AuditTable;

import java.time.LocalDateTime;

@Entity
@Table(name = "authz_policy")
@Audited
@AuditTable(value = "authz_policy_audit")
public class PolicyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission_id", nullable = false)
    private Long permissionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false)
    private SubjectType subjectType;

    /** Role name (e.g. "ACCOUNTANT") or user ID (e.g. "42") as a string reference. */
    @Column(name = "subject_id", nullable = false)
    private String subjectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyEffect effect;

    /** Condition AST as JSON text. NULL = unconditional. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "expression_json", columnDefinition = "jsonb")
    private String expressionJson;

    @Column(name = "use_custom_rego", nullable = false)
    private boolean useCustomRego = false;

    @Column(name = "custom_rego_snippet", columnDefinition = "TEXT")
    private String customRegoSnippet;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean deprecated = false;

    @Column(name = "disabled_reason")
    private String disabledReason;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_reason")
    private String deletedReason;

    public void setDeletedAt(java.time.LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public void setDeletedReason(String deletedReason) {
        this.deletedReason = deletedReason;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setDisabledReason(String disabledReason) {
        this.disabledReason = disabledReason;
    }


    public PolicyJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public SubjectType getSubjectType() {
        return subjectType;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public PolicyEffect getEffect() {
        return effect;
    }

    public String getExpressionJson() {
        return expressionJson;
    }

    public boolean isUseCustomRego() {
        return useCustomRego;
    }

    public String getCustomRegoSnippet() {
        return customRegoSnippet;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public String getDisabledReason() {
        return disabledReason;
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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public String getDeletedReason() {
        return deletedReason;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public void setSubjectType(SubjectType subjectType) {
        this.subjectType = subjectType;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public void setEffect(PolicyEffect effect) {
        this.effect = effect;
    }

    public void setExpressionJson(String expressionJson) {
        this.expressionJson = expressionJson;
    }

    public void setUseCustomRego(boolean useCustomRego) {
        this.useCustomRego = useCustomRego;
    }

    public void setCustomRegoSnippet(String customRegoSnippet) {
        this.customRegoSnippet = customRegoSnippet;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
