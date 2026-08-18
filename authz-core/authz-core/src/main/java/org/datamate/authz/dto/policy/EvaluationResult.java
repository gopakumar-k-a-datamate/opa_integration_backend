package org.datamate.authz.dto.policy;

import org.datamate.authz.exception.AuthzErrorCode;
import java.util.Collections;
import java.util.Map;

/**
 * Represents the rich result returned by the policy engine (e.g. OPA).
 * Contains the evaluation outcome, along with granular error codes and metadata
 * if the request was denied, allowing the framework to throw detailed exceptions.
 */
public record EvaluationResult(
        boolean allowed,
        AuthzErrorCode errorCode,
        String message,
        Map<String, Object> metadata
) {
    public static EvaluationResult granted() {
        return new EvaluationResult(true, null, null, Collections.emptyMap());
    }

    public static EvaluationResult denied(String defaultMessage) {
        return new EvaluationResult(false, AuthzErrorCode.DENIED, defaultMessage, Collections.emptyMap());
    }
}
