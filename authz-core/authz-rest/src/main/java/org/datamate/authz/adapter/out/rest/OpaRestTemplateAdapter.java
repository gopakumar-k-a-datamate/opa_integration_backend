package org.datamate.authz.adapter.out.rest;

import lombok.extern.slf4j.Slf4j;
import org.datamate.authz.application.dto.policy.OpaInputPayload;
import org.datamate.authz.application.port.out.policy.OpaEvaluationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;

import java.util.Map;

@Component
public class OpaRestTemplateAdapter implements OpaEvaluationPort {

    @EnableLogger
    private Logger log;

    private final RestTemplate restTemplate;
    private final String opaBaseUrl;

    public OpaRestTemplateAdapter(RestTemplateBuilder restTemplateBuilder,
                                  @Value("${authz.opa.url:http://localhost:8181}") String opaBaseUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.opaBaseUrl = opaBaseUrl;
    }

    @Override
    public boolean evaluate(String namespace, OpaInputPayload payload) {
        String url = String.format("%s/v1/data/app/authz/%s/allow", opaBaseUrl, namespace);
        
        try {
            log.debug("Sending evaluation request to OPA at URL: {}", url);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, payload, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object resultObj = response.getBody().get("result");
                if (resultObj instanceof Boolean) {
                    return (Boolean) resultObj;
                } else if (resultObj instanceof Map) {
                    // In case OPA returns {"result": {"allowed": true}}
                    Map<?, ?> resultMap = (Map<?, ?>) resultObj;
                    Object allowed = resultMap.get("allowed");
                    if (allowed instanceof Boolean) {
                        return (Boolean) allowed;
                    }
                }
            }
            log.warn("OPA returned non-200 or unparseable response: {}", response);
            return false;
        } catch (RestClientException e) {
            log.error("Failed to communicate with OPA sidecar at {}", url, e);
            // Fail-closed mechanism: if OPA is down, deny access.
            return false;
        }
    }
}
