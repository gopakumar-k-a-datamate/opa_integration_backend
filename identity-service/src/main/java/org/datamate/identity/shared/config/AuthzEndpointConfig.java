package org.datamate.identity.shared.config;

import org.datamate.authz.api.endpoint.AuthzBeans;
import org.datamate.authz.api.endpoint.EndpointAuthorization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class AuthzEndpointConfig {

    /**
     * Common authorization rule: only POLICY_ADMIN can manage policies.
     * Reused across all activated endpoints.
     */
    private final EndpointAuthorization policyAdminAuth = context -> {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            boolean hasAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("POLICY_ADMIN") ||
                                   a.getAuthority().equals("ROLE_POLICY_ADMIN"));
            if (!hasAdmin) {
                throw new AccessDeniedException("Access Denied: POLICY_ADMIN authority required");
            }
        }
    };

    @Bean(AuthzBeans.FIELDS)
    public EndpointAuthorization fieldsAuth()       { return policyAdminAuth; }

    @Bean(AuthzBeans.POLICIES)
    public EndpointAuthorization policiesAuth()      { return policyAdminAuth; }

    @Bean(AuthzBeans.SAVE_POLICIES)
    public EndpointAuthorization savePoliciesAuth()  { return policyAdminAuth; }

    @Bean(AuthzBeans.NAMESPACES)
    public EndpointAuthorization namespacesAuth()    { return policyAdminAuth; }

    // BUNDLE — auto-configured by the starter (open or API-key protected)
    // SUBJECTS — auto-configured by the starter (open by default)
}
