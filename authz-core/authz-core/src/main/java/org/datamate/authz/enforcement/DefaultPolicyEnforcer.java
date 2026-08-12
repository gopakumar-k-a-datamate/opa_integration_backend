package org.datamate.authz.enforcement;


import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.authz.api.policy.PolicyEvaluationClient;
import org.datamate.authz.annotation.PolicyField;
import org.datamate.authz.annotation.PolicyResource;
import org.springframework.context.annotation.Lazy;
import org.datamate.authz.api.principal.PrincipalProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnBean(PolicyEvaluationClient.class)
public class DefaultPolicyEnforcer implements PolicyEnforcer {

    @EnableLogger
    private Logger log;

    private final PolicyEvaluationClient policyEvaluationClient;
    private final PrincipalProvider principalProvider;

    public DefaultPolicyEnforcer(@Lazy PolicyEvaluationClient policyEvaluationClient, PrincipalProvider principalProvider) {
        this.policyEvaluationClient = policyEvaluationClient;
        this.principalProvider = principalProvider;
    }

    @Override
    public boolean supports(Object resource) {
        return resource != null && resource.getClass().isAnnotationPresent(PolicyResource.class);
    }

    private AuthorizationContext buildContext(String permissionCode, Map<String, Object> resourceContext) {
        String userId = principalProvider.getUserId();
        List<String> roles = principalProvider.getRoles();
        
        return AuthorizationContext.of(userId, roles, permissionCode, resourceContext);
    }

    @Override
    public boolean evaluate(Object resource) {
        if (!supports(resource)) {
            log.debug("Resource {} is not annotated with @PolicyResource. Bypassing policy evaluation.", 
                    resource != null ? resource.getClass().getName() : "null");
            return true;
        }

        PolicyResource resourceAnnotation = resource.getClass().getAnnotation(PolicyResource.class);

        // 1. Build Permission Code
        String permissionCode = String.format("%s:%s:%s", 
                resourceAnnotation.namespace(), 
                resourceAnnotation.resourceName(), 
                resourceAnnotation.action());

        // 3. Extract Resource Context via @PolicyField
        Map<String, Object> resourceContext = new HashMap<>();
        for (Field field : resource.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(PolicyField.class)) {
                field.setAccessible(true);
                try {
                    Object value = field.get(resource);
                    resourceContext.put(field.getName(), value);
                } catch (IllegalAccessException e) {
                    log.warn("Failed to extract PolicyField '{}' from Command", field.getName(), e);
                }
            }
        }

        // 4. Construct generic AuthorizationContext (engine-agnostic)
        AuthorizationContext context = buildContext(permissionCode, resourceContext);

        // 5. Evaluate Policy via the engine adapter
        log.debug("Evaluating policy for permission: {}", permissionCode);
        return policyEvaluationClient.evaluate(resourceAnnotation.namespace(), context);
    }

    @Override
    public boolean evaluate(String permissionCode) {
        String[] parts = permissionCode.split(":");
        if (parts.length == 0) {
            throw new IllegalArgumentException("Invalid permission code: " + permissionCode);
        }
        
        AuthorizationContext context = buildContext(permissionCode, new HashMap<>());
        log.debug("Evaluating RBAC policy for permission: {}", permissionCode);
        return policyEvaluationClient.evaluate(parts[0], context);
    }

    @Override
    public void enforce(Object resource) {
        if (!evaluate(resource)) {
            PolicyResource resourceAnnotation = resource != null ? resource.getClass().getAnnotation(PolicyResource.class) : null;
            String permissionCode = resourceAnnotation != null 
                    ? String.format("%s:%s:%s", resourceAnnotation.namespace(), resourceAnnotation.resourceName(), resourceAnnotation.action())
                    : "unknown";
            log.warn("Access Denied attempting {}", permissionCode);
            throw new AccessDeniedException("Access Denied: You do not have permission to perform this action.");
        }
    }

    @Override
    public void enforce(String permissionCode) {
        if (!evaluate(permissionCode)) {
            log.warn("Access Denied attempting {}", permissionCode);
            throw new AccessDeniedException("Access Denied: You do not have permission to perform this action.");
        }
    }
}
