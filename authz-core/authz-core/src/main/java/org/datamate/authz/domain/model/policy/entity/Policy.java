package org.datamate.authz.domain.model.policy.entity;

import lombok.Getter;

import org.datamate.authz.domain.model.policy.enumtype.SubjectType;
import org.datamate.authz.domain.model.policy.enumtype.PolicyEffect;

import java.time.LocalDateTime;

/**
 * The core authorization rule for this application module.
 *
 * <p>Maps a global subject (Role or User from the Identity Provider) to a local permission,
 * with an optional conditional expression stored as a JSON AST.</p>
 *
 * <h3>Domain Rules</h3>
 * <ul>
 *   <li>A {@code null} {@code expressionJson} means the policy is <b>unconditional</b>.</li>
 *   <li>{@code enabled = false} → excluded from Rego compilation, no runtime effect.</li>
 *   <li>{@link PolicyEffect#DENY} always overrides {@link PolicyEffect#ALLOW}.</li>
 *   <li>Multiple ALLOW policies for the same subject+permission → most permissive wins.</li>
 * </ul>
 *
 * <h3>Domain Behavior</h3>
 * <ul>
 *   <li>{@link #isUnconditional()} — policy applies to all requests matching subject+permission.</li>
 *   <li>{@link #isAllow()} / {@link #isDeny()} — effect checks.</li>
 *   <li>{@link #isRolePolicy()} / {@link #isUserPolicy()} — subject type checks.</li>
 *   <li>{@link #isEffective()} — policy is active and has runtime impact.</li>
 * </ul>
 */
@Getter
public class Policy {
    private final Long id;
    private final Long permissionId;
    private final SubjectType subjectType;

    /**
     * The role name (e.g. {@code "ACCOUNTANT"}) or user ID (e.g. {@code "42"}).
     * Stored as a string reference — the actual record lives in the Identity Provider DB.
     */
    private final String subjectId;
    private final PolicyEffect effect;

    /** Condition AST as JSON text. {@code null} = unconditional policy. */
    private final String expressionJson;

    /** {@code false} when auto-disabled due to a deprecated condition field. */
    private final boolean enabled;

    /** Reason this policy was auto-disabled, or {@code null} if it is enabled. */
    private final String disabledReason;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public Policy(Long id, Long permissionId, SubjectType subjectType, String subjectId,
                       PolicyEffect effect, String expressionJson, boolean enabled,
                       String disabledReason, LocalDateTime createdAt,
                       LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.permissionId = permissionId;
        this.subjectType = subjectType;
        this.subjectId = subjectId;
        this.effect = effect;
        this.expressionJson = expressionJson;
        this.enabled = enabled;
        this.disabledReason = disabledReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    // ── Domain Behavior ────────────────────────────────────────────────────────

    /** Returns {@code true} when this policy has not been soft-deleted. */
    public boolean isActive() {
        return deletedAt == null;
    }

    /**
     * Returns {@code true} when this policy is active AND enabled —
     * i.e. it has a real runtime impact on authorization decisions.
     */
    public boolean isEffective() {
        return isActive() && enabled;
    }

    /** Returns {@code true} when the condition is unconditional (applies to all resource states). */
    public boolean isUnconditional() {
        return expressionJson == null || expressionJson.isBlank();
    }

    /** Returns {@code true} if this policy grants access. */
    public boolean isAllow() {
        return effect == PolicyEffect.ALLOW;
    }

    /** Returns {@code true} if this policy blocks access. DENY always overrides ALLOW. */
    public boolean isDeny() {
        return effect == PolicyEffect.DENY;
    }

    /** Returns {@code true} when the subject is a global Role (from the Identity Provider). */
    public boolean isRolePolicy() {
        return subjectType == SubjectType.ROLE;
    }

    /** Returns {@code true} when the policy is scoped to a specific User ID. */
    public boolean isUserPolicy() {
        return subjectType == SubjectType.USER;
    }
}


