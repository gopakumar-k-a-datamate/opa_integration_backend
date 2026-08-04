package org.datamate.authz.starter.enforcement;

import org.datamate.authz.enforcement.PolicyEnforcer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.authz.dto.policy.EvaluationPayload;
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
import java.util.stream.Collectors;

@Component
public class SpringSecurityPolicyEnforcer implements PolicyEnforcer {

    @EnableLogger
    private Logger log;

    private final PolicyEvaluationClient policyEvaluationClient;
    private final ObjectMapper objectMapper;

    public SpringSecurityPolicyEnforcer(@Lazy PolicyEvaluationClient policyEvaluationClient, ObjectMapper objectMapper) {
        this.policyEvaluationClient = policyEvaluationClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(Object resource) {
        return resource != null && resource.getClass().isAnnotationPresent(PolicyResource.class);
    }

    @Override
    public boolean evaluate(Object resource) {
        if (!supports(resource)) {
            log.debug("Resource {} is not annotated with @PolicyResource. Bypassing OPA evaluation.", 
                    resource != null ? resource.getClass().getName() : "null");
            return true;
        }

        PolicyResource resourceAnnotation = resource.getClass().getAnnotation(PolicyResource.class);

        // 1. Build Permission Code
        String permissionCode = String.format("%s:%s:%s", 
                resourceAnnotation.namespace(), 
                resourceAnnotation.resourceName(), 
                resourceAnnotation.action());

        // 2. Extract User Details from Authentication or JWT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = "anonymous";
        List<String> roles = new ArrayList<>();
        
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            userId = authentication.getName();
            roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                    .collect(Collectors.toList());
        } else {
            // Attempt to parse JWT manually from Authorization header
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    HttpServletRequest request = attrs.getRequest();
                    String authHeader = request.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        String[] parts = token.split("\\.");
                        if (parts.length == 3) {
                            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                            JsonNode json = objectMapper.readTree(payload);
                            
                            if (json.has("userId")) userId = json.get("userId").asText();
                            
                            if (json.has("role") && json.get("role").isArray()) {
                                roles = new ArrayList<>();
                                for (JsonNode roleNode : json.get("role")) {
                                    roles.add(roleNode.asText());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to manually extract JWT from header", e);
            }
        }

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

        // 4. Construct Payload
        EvaluationPayload payload = EvaluationPayload.builder()
                .input(EvaluationPayload.Input.builder()
                        .user(EvaluationPayload.User.builder()
                                .id(userId)
                                .roles(roles)
                                .build())
                        .permission(permissionCode)
                        .resource(resourceContext)
                        .build())
                .build();

        // 5. Evaluate Policy against OPA
        log.debug("Evaluating policy for permission: {}", permissionCode);
        return policyEvaluationClient.evaluate(resourceAnnotation.namespace(), payload);
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
}
