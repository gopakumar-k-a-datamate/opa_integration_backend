package org.datamate.authz.starter.enforcement;

import org.datamate.authz.enforcement.PolicyEnforcer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.authz.enforcement.AuthorizationContext;
import org.datamate.authz.api.policy.PolicyEvaluationClient;
import org.datamate.authz.annotation.PolicyField;
import org.datamate.authz.annotation.PolicyResource;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.datamate.bedrock.framework.common.security.jwt.service.JwtTokenService;
import com.datamate.bedrock.framework.common.security.vo.TokenValidationResult;

@Component
public class SpringSecurityPolicyEnforcer implements PolicyEnforcer {

    @EnableLogger
    private Logger log;

    private final PolicyEvaluationClient policyEvaluationClient;
    private final ObjectMapper objectMapper;
    private final Optional<JwtTokenService> jwtTokenService;

    public SpringSecurityPolicyEnforcer(@Lazy PolicyEvaluationClient policyEvaluationClient, ObjectMapper objectMapper, Optional<JwtTokenService> jwtTokenService) {
        this.policyEvaluationClient = policyEvaluationClient;
        this.objectMapper = objectMapper;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public boolean supports(Object resource) {
        return resource != null && resource.getClass().isAnnotationPresent(PolicyResource.class);
    }

    private AuthorizationContext buildContext(String permissionCode, Map<String, Object> resourceContext) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // update anonymous as coming from security
        String userId = "anonymous";
        List<String> roles = new ArrayList<>();
        
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            userId = authentication.getName();
            roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                    .collect(Collectors.toList());
        } else {
            // Attempt to securely parse JWT manually from Authorization header using Bedrock security-jwt
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    HttpServletRequest request = attrs.getRequest();
                    String authHeader = request.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        
                        if (jwtTokenService.isPresent()) {
                            TokenValidationResult result = jwtTokenService.get().validateToken(token);
                            if (result.isValid()) {
                                String tokenType = jwtTokenService.get().getTokenType(token);
                                if (!"access".equalsIgnoreCase(tokenType)) {
                                    log.warn("Access Denied: Attempted to use a '{}' token for API authorization", tokenType);
                                } else {
                                    userId = result.userId();
                                    
                                    // Extract roles if present in claims
                                    if (result.claims() != null && result.claims().get("role") instanceof List) {
                                        roles = new ArrayList<>();
                                        for (Object roleObj : (List<?>) result.claims().get("role")) {
                                            roles.add(roleObj.toString());
                                        }
                                    }
                                }
                            } else {
                                log.warn("Manual JWT fallback validation failed: {}", result.getErrorMessage());
                            }
                        } else {
                            log.warn("JwtTokenService is not available. Cannot securely validate fallback token.");
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to manually extract JWT from header", e);
            }
        }
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
