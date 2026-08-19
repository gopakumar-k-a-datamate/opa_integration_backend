package org.datamate.authz.api.policy;

import org.datamate.authz.enforcement.AuthorizationContext;
import org.datamate.authz.dto.policy.EvaluationResult;

public interface PolicyEvaluationClient {
    /**
     * Evaluates the given authorization context against the policy engine.
     *
     * <p>
     * Implementations are responsible for translating the generic
     * {@link AuthorizationContext} into the engine-specific request format
     * (e.g. OPA's {@code input} JSON payload).
     *
     * @param namespace The namespace of the resource (e.g. "finance").
     * @param context   The generic authorization context (user, roles, permission,
     *                  resource).
     * @return EvaluationResult containing true if the policy engine allows the request, false otherwise with reason.
     */
    EvaluationResult evaluate(String namespace, AuthorizationContext context);
}
