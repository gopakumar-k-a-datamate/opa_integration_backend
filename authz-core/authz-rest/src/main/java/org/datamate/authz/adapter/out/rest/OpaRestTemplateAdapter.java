package org.datamate.authz.adapter.out.rest;

import lombok.extern.slf4j.Slf4j;
import org.datamate.authz.application.dto.policy.OpaInputPayload;
import org.datamate.authz.application.port.out.policy.OpaEvaluationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.datamate.authz.shared.exception.OpaConfigurationException;
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
    private final String evaluationUrl;

    public OpaRestTemplateAdapter(RestTemplateBuilder restTemplateBuilder,
                                  @Value("${authz.opa.config.file:opa-config.yaml}") String opaConfigFile) {
        this.restTemplate = restTemplateBuilder.build();
        
        // Parse opa-config.yaml to extract evaluation_url
        try {
            YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
            yamlFactory.setResources(new FileSystemResource(opaConfigFile));
            java.util.Properties properties = yamlFactory.getObject();
            
            if (properties != null && properties.getProperty("evaluation_url") != null) {
                this.evaluationUrl = properties.getProperty("evaluation_url");
            } else {
                throw new OpaConfigurationException("Required property 'evaluation_url' is missing in " + opaConfigFile);
            }
        } catch (Exception e) {
            if (e instanceof OpaConfigurationException) throw e;
            throw new OpaConfigurationException("Failed to load OPA configuration from " + opaConfigFile + ". Please ensure the file exists and contains the 'evaluation_url' property.", e);
        }
    }

    @Override
    public boolean evaluate(String namespace, OpaInputPayload payload) {
        String url = String.format(evaluationUrl, namespace);
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
