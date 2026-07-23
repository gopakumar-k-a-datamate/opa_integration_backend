package org.datamate.authz.application.dto.policy;

import com.fasterxml.jackson.databind.JsonNode;
import org.datamate.authz.domain.model.policy.enumtype.PolicyEffect;


/**
 * One row in the Admin UI's Role-Permission Grid.
 *
 * <p>If no policy exists for this permission + subject, {@code policyId},
 * {@code effect}, {@code expressionJson}, and {@code disabledReason} will be {@code null},
 * and {@code enabled} will be {@code false}.</p>
 */
public record PolicyGridItemDto(
        String permissionCode,
        String action,
        String namespace,
        String resourceName,
        Long policyId,
        PolicyEffect effect,
        JsonNode expressionJson,
        boolean enabled,
        String disabledReason,
        String deletedReason,
        boolean deprecated
) {}


