package org.datamate.authz.rest.dto;

import com.fasterxml.jackson.databind.JsonNode;
import org.datamate.authz.model.policy.enumtype.PolicyEffect;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
 * @param denialMessage   Optional human-readable message shown to users when this policy causes a denial.
 *                        Baked into the OPA bundle at compile time. {@code null} = generic fallback.
 */
public record PolicyItemRequest(
        @NotBlank(message = "permissionCode is required") String permissionCode,
        @NotNull(message = "effect is required") PolicyEffect effect,
        JsonNode expressionJson,
        boolean enabled,
        boolean isDeleted,
        String deletedReason,
        String disabledReason,
        boolean useCustomRego,
        String customRegoSnippet,
        @Size(max = 500, message = "denialMessage must not exceed 500 characters") String denialMessage
) {}




