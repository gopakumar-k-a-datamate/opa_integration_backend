package org.datamate.authz.domain.model.policy.enumtype;

/**
 * Lifecycle status of an {@code authz_condition_field}.
 *
 * <p>When a field is removed from the application code, the diff-sync logic checks
 * for policy references. If any exist, the field is marked {@link #DEPRECATED} and
 * the affected policies are auto-disabled. If no policies reference it, the field
 * is soft-deleted instead.</p>
 */
public enum FieldStatus {
    ACTIVE,
    DEPRECATED
}


