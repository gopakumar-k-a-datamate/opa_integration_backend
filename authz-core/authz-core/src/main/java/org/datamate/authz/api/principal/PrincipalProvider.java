package org.datamate.authz.api.principal;

import java.util.List;

/**
 * Interface for providing the current principal's identity details.
 * <p>
 * Implementations of this interface should hook into the underlying security
 * framework (e.g., Spring Security) to extract the authenticated user's ID
 * and roles. This decouples the authorization engine from specific security
 * context implementations.
 */
public interface PrincipalProvider {

    /**
     * Gets the unique identifier of the current principal.
     *
     * @return the user ID, or {@code null} if unauthenticated.
     */
    String getUserId();

    /**
     * Gets the roles or authorities assigned to the current principal.
     *
     * @return a list of roles, or an empty list if none are assigned.
     */
    List<String> getRoles();
}
