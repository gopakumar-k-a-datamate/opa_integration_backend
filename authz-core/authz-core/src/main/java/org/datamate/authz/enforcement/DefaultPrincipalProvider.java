package org.datamate.authz.enforcement;

import org.datamate.authz.api.principal.PrincipalProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link PrincipalProvider} that extracts identity
 * details from the Spring Security context.
 */
@Component
@ConditionalOnMissingBean(PrincipalProvider.class)
public class DefaultPrincipalProvider implements PrincipalProvider {

    @EnableLogger
    private Logger log;

    @Override
    public String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        log.warn("Authentication is missing from SecurityContext; unable to extract getUserId()");
        return null;
    }

    @Override
    public List<String> getRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                    .collect(Collectors.toList());
        }
        log.warn("Authentication or authorities are missing; returning empty roles");
        return new ArrayList<>();
    }
}
