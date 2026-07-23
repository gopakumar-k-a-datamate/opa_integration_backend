package org.datamate.authz.application.port.out.policy;

import org.datamate.authz.application.dto.policy.OpaInputPayload;

public interface OpaEvaluationPort {
    /**
     * Evaluates the given input payload against the specified namespace's OPA bundle.
     *
     * @param namespace The namespace of the resource (e.g. "finance").
     * @param payload   The input payload containing user, permission, and resource context.
     * @return true if OPA allows the request, false otherwise.
     */
    boolean evaluate(String namespace, OpaInputPayload payload);
}
