package org.datamate.authz.enforcement;

import org.datamate.authz.api.principal.PrincipalProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link PrincipalProvider} that extracts identity
 * details from the Spring Security context.
 */
public class DefaultPrincipalProvider implements PrincipalProvider {

    @EnableLogger
    private Logger log;

    @Override
    public String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal != null) {
                try {
                    // Bedrock's UserDetails record provides getUserId() and userId()
                    Method getUserIdMethod = null;
                    try {
                        getUserIdMethod = principal.getClass().getMethod("getUserId");
                    } catch (NoSuchMethodException e) {
                        try {
                            getUserIdMethod = principal.getClass().getMethod("userId");
                        } catch (NoSuchMethodException e2) {
                            // ignore
                        }
                    }
                    
                    if (getUserIdMethod != null) {
                        Object id = getUserIdMethod.invoke(principal);
                        if (id != null) {
                            return id.toString();
                        }
                    }
                } catch (Exception e) {
                    log.debug("Principal does not have getUserId() method, falling back to getName()");
                }
            }
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
