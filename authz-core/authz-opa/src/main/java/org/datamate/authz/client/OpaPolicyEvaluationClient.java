package org.datamate.authz.client;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.authz.api.policy.PolicyEvaluationClient;
import org.datamate.authz.enforcement.AuthorizationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import org.datamate.authz.dto.policy.EvaluationResult;

public class OpaPolicyEvaluationClient implements PolicyEvaluationClient {

    @EnableLogger
    private Logger log;

    private final RestTemplate restTemplate;
    private final String opaEvaluationBaseUrl;

    public OpaPolicyEvaluationClient(
            RestTemplate restTemplate,
            String opaBaseUrl) {
        this.restTemplate = restTemplate;
        this.opaEvaluationBaseUrl = opaBaseUrl + "/v1/data/";
    }

    @Override
    public EvaluationResult evaluate(String namespace, AuthorizationContext context) {
        String url = opaEvaluationBaseUrl + namespace;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("user", context.userId());
        inputMap.put("roles", context.roles());
        inputMap.put("permission", context.permissionCode());
        inputMap.put("resource", context.resourceData());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", inputMap);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            log.debug("Sending authorization context to OPA at URL: {}", url);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (body.containsKey("result")) {
                    Object result = body.get("result");
                    if (result instanceof Map) {
                        Map<String, Object> resultMap = (Map<String, Object>) result;
                        if (resultMap.containsKey("allow")) {
                            boolean allow = Boolean.TRUE.equals(resultMap.get("allow"));
                            log.debug("OPA evaluated policy for permission '{}': allow={}", context.permissionCode(), allow);
                            if (allow) {
                                return EvaluationResult.granted();
                            } else {
                                String reason = (String) resultMap.getOrDefault("reason", "Access Denied: You do not have permission to perform this action.");
                                return EvaluationResult.denied(reason);
                            }
                        }
                    }
                    if (result instanceof Boolean) {
                        boolean allow = (Boolean) result;
                        log.debug("OPA evaluated policy for permission '{}': allow={}", context.permissionCode(), allow);
                        if (allow) {
                            return EvaluationResult.granted();
                        } else {
                            return EvaluationResult.denied("Access Denied: You do not have permission to perform this action.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to evaluate policy with OPA for namespace '{}'", namespace, e);
        }
        
        log.warn("OPA evaluation returned false/denied (or failed) for permission '{}'", context.permissionCode());
        return EvaluationResult.denied("Access Denied: You do not have permission to perform this action.");
    }
}
