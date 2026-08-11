package org.datamate.authz.enforcement;

/**
 * Public framework SDK contract for evaluating and enforcing OPA policy rules
 * against domain resource objects annotated with @PolicyResource.
 */
public interface PolicyEnforcer {

    /**
     * Evaluates whether the current principal is authorized to perform the action on the given resource.
     *
     * @param resource The domain resource command or object (should be annotated with @PolicyResource).
     * @return true if OPA policy permits execution, false otherwise.
     */
    boolean evaluate(Object resource);

    /**
     * Enforces policy authorization, throwing an AccessDeniedException if the current principal
     * is unauthorized to perform the action on the given resource.
     *
     * @param resource The domain resource command or object.
     * @throws org.datamate.authz.exception.AuthzDeniedException if authorization fails.
     */
    void enforce(Object resource);

    /**
     * Checks whether the given object is a protected policy resource supported by this enforcer
     * (i.e., whether its class is annotated with @PolicyResource).
     *
     * @param resource The object to check.
     * @return true if the resource is protected and supported, false otherwise.
     */
    boolean supports(Object resource);

    /**
     * Evaluates whether the current principal is authorized for the given permission string.
     *
     * @param permissionCode The permission string (e.g., "namespace:resource:action").
     * @return true if OPA policy permits execution, false otherwise.
     */
    boolean evaluate(String permissionCode);

    /**
     * Enforces policy authorization for the given permission string, throwing an 
     * AccessDeniedException if the current principal is unauthorized.
     *
     * @param permissionCode The permission string (e.g., "namespace:resource:action").
     */
    void enforce(String permissionCode);
}
