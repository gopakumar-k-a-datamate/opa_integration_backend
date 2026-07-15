package org.datamate.authz.domain.model.policy.enumtype;

/**
 * The effect of an authorization policy.
 *
 * <p>DENY always overrides ALLOW — any matching DENY policy blocks access
 * regardless of all matching ALLOW policies.</p>
 */
public enum PolicyEffect {
    ALLOW,
    DENY
}


