package org.datamate.authz.application.dto.policy;

import com.fasterxml.jackson.databind.JsonNode;
import org.datamate.authz.domain.model.policy.enumtype.PolicyEffect;

/**
 * A single policy entry inside a {@link SavePoliciesRequest}.
 *
 * @param permissionCode  e.g. {@code "finance:journal:create"}
 * @param effect          {@code ALLOW} or {@code DENY}
 * @param expressionJson  Condition AST as JSON, or {@code null} for unconditional
 * @param enabled         Whether the policy should be active
 * @param isDeleted       If {@code true}, the matching policy will be soft-deleted
 * @param deletedReason   Reason for deletion
 * @param disabledReason  Reason for disabling
 */
public record PolicyItemRequest(
        String permissionCode,
        PolicyEffect effect,
        JsonNode expressionJson,
        boolean enabled,
        boolean isDeleted,
        String deletedReason,
        String disabledReason
) {}



