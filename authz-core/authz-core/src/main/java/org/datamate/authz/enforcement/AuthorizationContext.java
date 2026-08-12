package org.datamate.authz.enforcement;

import java.util.List;
import java.util.Map;

/**
 * Generic, engine-agnostic authorization context.
 *
 * <p>This is the common contract between the {@link PolicyEnforcer} and the
 * {@link org.datamate.authz.application.port.out.PolicyEvaluationClientPort}. It carries the
 * minimum information needed to make an allow/deny decision, without any coupling
 * to OPA's specific JSON payload structure.</p>
 *
 * <p>Adapter implementations (e.g. {@code RestPolicyEvaluationClient}) are
 * responsible for mapping this generic context into the engine-specific request format.</p>
 *
 * <p>Use {@link #of(String, List, String, Map)} to create instances.</p>
 */
public record AuthorizationContext(
        String userId,
        List<String> roles,
        String permissionCode,
        Map<String, Object> resourceData
) {
    /**
     * Creates a new {@code AuthorizationContext} with the given user, permission, and resource data.
     *
     * @param userId         the authenticated user's identifier
     * @param roles          the list of roles assigned to the user
     * @param permissionCode the permission code being evaluated (e.g. {@code finance:journal:create})
     * @param resourceData   the resource context attributes for attribute-based conditions
     * @return a new {@code AuthorizationContext} instance
     */
    public static AuthorizationContext of(String userId, List<String> roles,
                                          String permissionCode, Map<String, Object> resourceData) {
        return new AuthorizationContext(userId, roles, permissionCode, resourceData);
    }
}
