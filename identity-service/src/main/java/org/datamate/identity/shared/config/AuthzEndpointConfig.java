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
     * Common authorization rule: only SECURITY_ADMIN can manage policies.
     * Reused across all activated endpoints.
     */
    private final EndpointAuthorization securityAdminOnly = context -> {
        // need to add authorization here
    };

    @Bean(AuthzBeans.FIELDS)
    public EndpointAuthorization fieldsAuth()       { return securityAdminOnly; }

    @Bean(AuthzBeans.POLICIES)
    public EndpointAuthorization policiesAuth()      { return securityAdminOnly; }

    @Bean(AuthzBeans.SAVE_POLICIES)
    public EndpointAuthorization savePoliciesAuth()  { return securityAdminOnly; }

    @Bean(AuthzBeans.NAMESPACES)
    public EndpointAuthorization namespacesAuth()    { return securityAdminOnly; }

    // BUNDLE — auto-configured by the starter (open or API-key protected)
    // SUBJECTS — auto-configured by the starter (open by default)
}
