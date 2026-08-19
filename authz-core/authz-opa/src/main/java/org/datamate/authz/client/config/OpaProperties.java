package org.datamate.authz.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "authz.opa")
public class OpaProperties {

    /**
     * URL for OPA Policy Validation API.
     */
    private String validationUrl = "http://localhost:8181";

    /**
     * URL for OPA Data Evaluation API.
     */
    private String evaluationUrl = "http://localhost:8181";

    public String getValidationUrl() {
        return validationUrl;
    }

    public void setValidationUrl(String validationUrl) {
        this.validationUrl = validationUrl;
    }

    public String getEvaluationUrl() {
        return evaluationUrl;
    }

    public void setEvaluationUrl(String evaluationUrl) {
        this.evaluationUrl = evaluationUrl;
    }
}
