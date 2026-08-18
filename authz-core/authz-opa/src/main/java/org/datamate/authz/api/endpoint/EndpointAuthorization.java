package org.datamate.authz.api.endpoint;

/**
 * Single extension contract for all authorization endpoints.
 * <p>
 * Consumers implement this interface as named beans (e.g., "policiesAuthorization")
 * to conditionally activate library-provided REST controllers and provide
 * their own security logic.
 * </p>
 */
@FunctionalInterface
public interface EndpointAuthorization {

    /**
     * Determine whether the current request is authorized to proceed.
     *
     * @param context the authorization context containing request details
     * @throws org.springframework.security.access.AccessDeniedException (or similar) if denied
     */
    void authorize(AuthorizationContext context);
}
