package org.datamate.authz.client;
// TODO:- update path. Follow standards

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.datamate.authz.api.policy.PolicyValidation;
import org.datamate.authz.api.policy.PolicyValidation;
import org.datamate.authz.model.policy.valueobject.RegoValidationError;
import org.datamate.authz.model.policy.valueobject.RegoValidationResult;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class OpaPolicyValidator implements PolicyValidation {

    @EnableLogger
    Logger log;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String opaPolicyBaseUrl;

    private static final int WRAPPER_LINE_OFFSET = 4;

    // Todo: Don't set url as hardcoded in adapter layer
    public OpaPolicyValidator(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            String opaBaseUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.opaPolicyBaseUrl = opaBaseUrl + "/v1/policies/";
    }

    @Override
    public RegoValidationResult validate(String regoSnippet) {
        String policyId = "_validation_temp_policy";
        String fullModule = buildTemporaryModule(policyId, regoSnippet);
        String url = opaPolicyBaseUrl + policyId;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            HttpEntity<String> request = new HttpEntity<>(fullModule, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.PUT, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return RegoValidationResult.success();
            }

            return RegoValidationResult.failure(List.of(
                    new RegoValidationError(0, 0, "Unexpected OPA response: " + response.getStatusCode())
            ));

        } catch (HttpClientErrorException.BadRequest e) {
            return parseOpaErrors(e.getResponseBodyAsString());
        } catch (RestClientException e) {
            log.error("OPA validation endpoint unreachable at {}", url, e);
            return RegoValidationResult.failure(List.of(
                    new RegoValidationError(0, 0,
                            "Rego validation service unavailable. Cannot verify syntax.")
            ));
        } finally {
            cleanupTemporaryPolicy(url);
        }
    }

    private String buildTemporaryModule(String policyId, String snippet) {
        if (snippet != null && snippet.trim().startsWith("package ")) {
            // Rewrite the package statement to avoid bundle ownership conflicts during validation
            return snippet.replaceFirst("(?m)^package\\s+[\\w\\.]+", "package " + policyId);
        }
        return "package " + policyId + "\n"
                + "\n"
                + "import rego.v1\n"
                + "\n"
                + snippet;
    }

    private RegoValidationResult parseOpaErrors(String responseBody) {
        List<RegoValidationError> errors = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode errorsNode = root.get("errors");

            if (errorsNode != null && errorsNode.isArray()) {
                for (JsonNode errorNode : errorsNode) {
                    String message = errorNode.has("message")
                            ? errorNode.get("message").asText()
                            : "Unknown syntax error";

                    int line = 0;
                    int column = 0;
                    JsonNode location = errorNode.get("location");
                    if (location != null) {
                        line = Math.max(0, location.path("row").asInt(0) - WRAPPER_LINE_OFFSET);
                        column = location.path("col").asInt(0);
                    }

                    errors.add(new RegoValidationError(line, column, message));
                }
            }

            if (errors.isEmpty()) {
                String message = root.has("message")
                        ? root.get("message").asText()
                        : "Rego syntax error";
                errors.add(new RegoValidationError(0, 0, message));
            }

        } catch (Exception e) {
            log.warn("Failed to parse OPA error response: {}", responseBody, e);
            errors.add(new RegoValidationError(0, 0, "Rego syntax error (unparseable details)"));
        }

        return RegoValidationResult.failure(errors);
    }

    private void cleanupTemporaryPolicy(String url) {
        try {
            restTemplate.delete(url);
        } catch (HttpClientErrorException.NotFound e) {
            // Expected
        } catch (RestClientException e) {
            log.warn("Failed to cleanup temporary OPA policy at {}. Non-fatal.", url, e);
        }
    }
}
