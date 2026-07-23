package org.datamate.authz.application.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.datamate.authz.application.dto.policy.OpaInputPayload;
import org.datamate.authz.application.port.out.policy.OpaEvaluationPort;
import org.datamate.authz.shared.annotation.PolicyField;
import org.datamate.authz.shared.annotation.PolicyResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Base64;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
public class PolicyEnforcementAspect {


    private final OpaEvaluationPort opaEvaluationPort;

    public PolicyEnforcementAspect(@org.springframework.context.annotation.Lazy OpaEvaluationPort opaEvaluationPort) {
        this.opaEvaluationPort = opaEvaluationPort;
    }

    // Intercept any method that takes a Command object annotated with @PolicyResource.
    // We use a broad pointcut and then filter dynamically.
    @Around("execution(* org.datamate..*(..))")
    public Object enforcePolicy(ProceedingJoinPoint joinPoint) throws Throwable {
        
        Object command = null;
        PolicyResource resourceAnnotation = null;

        // Find the argument annotated with @PolicyResource
        for (Object arg : joinPoint.getArgs()) {
            if (arg != null) {
                resourceAnnotation = arg.getClass().getAnnotation(PolicyResource.class);
                if (resourceAnnotation != null) {
                    command = arg;
                    break;
                }
            }
        }

        // If no protected command is found, just proceed
        if (resourceAnnotation == null) {
            return joinPoint.proceed();
        }

        // 1. Build Permission Code
        String permissionCode = String.format("%s:%s:%s", 
                resourceAnnotation.namespace(), 
                resourceAnnotation.name(), 
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
                            JsonNode json = new ObjectMapper().readTree(payload);
                            
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
        for (Field field : command.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(PolicyField.class)) {
                field.setAccessible(true);
                try {
                    Object value = field.get(command);
                    resourceContext.put(field.getName(), value);
                } catch (IllegalAccessException e) {
                    log.warn("Failed to extract PolicyField '{}' from Command", field.getName(), e);
                }
            }
        }

        // 4. Construct Payload
        OpaInputPayload payload = OpaInputPayload.builder()
                .input(OpaInputPayload.Input.builder()
                        .user(OpaInputPayload.User.builder()
                                .id(userId)
                                .roles(roles)
                                .build())
                        .permission(permissionCode)
                        .resource(resourceContext)
                        .build())
                .build();

        // 5. Evaluate Policy against OPA
        log.debug("Evaluating policy for permission: {}", permissionCode);
        boolean isAllowed = opaEvaluationPort.evaluate(resourceAnnotation.namespace(), payload);

        if (!isAllowed) {
            log.warn("Access Denied for user {} attempting {}", userId, permissionCode);
            throw new AccessDeniedException("Access Denied: You do not have permission to perform this action.");
        }

        return joinPoint.proceed();
    }
}
