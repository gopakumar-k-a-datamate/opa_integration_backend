package org.datamate.authz.adapter.out.persistence.policy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.datamate.authz.domain.model.policy.enumtype.PolicyEffect;
import org.datamate.authz.domain.model.policy.enumtype.SubjectType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "authz_policy")
@Getter
@Setter
@NoArgsConstructor
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

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "disabled_reason")
    private String disabledReason;

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
}


