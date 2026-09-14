package org.datamate.authz.api.endpoint;

/**
 * Constants for the named EndpointAuthorization beans used to activate
 * library-provided REST endpoints.
 * <p>
 * Consumers should use these constants when defining their @Bean methods
 * (e.g., @Bean(AuthzBeans.POLICIES)).
 * </p>
 */
public final class AuthzBeans {

    public static final String FIELDS = "fieldsAuthorization";
    public static final String POLICIES = "policiesAuthorization";
    public static final String SAVE_POLICIES = "savePoliciesAuthorization";
    public static final String NAMESPACES = "namespacesAuthorization";
    public static final String SUBJECTS = "subjectsAuthorization";
    public static final String BUNDLE = "bundleAuthorization";

    private AuthzBeans() {
        // Prevent instantiation
    }
}
