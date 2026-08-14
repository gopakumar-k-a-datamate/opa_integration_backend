package org.datamate.authz.rest.client;

import org.datamate.authz.enforcement.AuthorizationContext;
import org.datamate.authz.dto.policy.EvaluationPayload;
import org.datamate.authz.api.policy.PolicyEvaluationClient;
import org.datamate.authz.dto.policy.EvaluationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.datamate.authz.exception.AuthzEngineConfigurationException;


import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;

import java.util.Map;

public class RestPolicyEvaluationClient implements PolicyEvaluationClient {

    @EnableLogger
    private Logger log;

    private final RestTemplate restTemplate;
    private final String evaluationUrl;

    public RestPolicyEvaluationClient(RestTemplateBuilder restTemplateBuilder, String evaluationUrl) {
        this.restTemplate = restTemplateBuilder.build();
        if (evaluationUrl == null || evaluationUrl.isBlank()) {
            throw new AuthzEngineConfigurationException("Required property 'evaluation_url' is missing.");
        }
        this.evaluationUrl = evaluationUrl;
    }

    @Override
    public EvaluationResult evaluate(String namespace, AuthorizationContext context) {
        // Map the generic AuthorizationContext into OPA's specific input payload format
        EvaluationPayload payload = EvaluationPayload.of(
                EvaluationPayload.Input.of(
                        EvaluationPayload.User.of(
                                context.userId(),
                                context.roles()),
                        context.permissionCode(),
                        context.resourceData()));

        String url = String.format(evaluationUrl, namespace);
        try {
            log.debug("Sending evaluation request to OPA at URL: {}", url);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, payload, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object resultObj = response.getBody().get("result");
                if (resultObj instanceof Boolean) {
                    boolean allow = (Boolean) resultObj;
                    if (allow) {
                        return EvaluationResult.granted();
                    } else {
                        return EvaluationResult.denied("Access Denied: You do not have permission to perform this action.");
                    }
                } else if (resultObj instanceof Map) {
                    // In case OPA returns {"result": {"allowed": true}}
                    Map<String, Object> resultMap = (Map<String, Object>) resultObj;
                    Object allowedObj = resultMap.get("allowed");
                    
                    // Fallback to "allow" if "allowed" is not present
                    if (allowedObj == null) {
                        allowedObj = resultMap.get("allow");
                    }

                    if (allowedObj instanceof Boolean) {
                        boolean allow = (Boolean) allowedObj;
                        if (allow) {
                            return EvaluationResult.granted();
                        } else {
                            String reason = (String) resultMap.getOrDefault("reason", "Access Denied: You do not have permission to perform this action.");
                            return EvaluationResult.denied(reason);
                        }
                    }
                }
            }
            log.warn("OPA returned non-200 or unparseable response: {}", response);
            return EvaluationResult.denied("Access Denied: You do not have permission to perform this action.");
        } catch (RestClientException e) {
            log.error("Failed to communicate with OPA sidecar at {}", url, e);
            // Fail-closed mechanism: if OPA is down, deny access.
            return EvaluationResult.denied("Access Denied: You do not have permission to perform this action.");
        }
    }
}
