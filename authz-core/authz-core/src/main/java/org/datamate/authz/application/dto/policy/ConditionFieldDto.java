package org.datamate.authz.application.dto.policy;

import org.datamate.authz.domain.model.policy.enumtype.FieldType;

import java.util.List;

/**
 * A condition field descriptor returned by the Condition Builder endpoint.
 *
 * @param fieldName       Internal field name used in the condition AST, e.g. {@code "amount"}
 * @param fieldType       Data type controlling available operators
 * @param displayName     Human-readable label shown in the UI
 * @param allowedValues   Static dropdown options, {@code null} if not applicable
 * @param optionsEndpoint Dynamic endpoint for live dropdown, {@code null} if not applicable
 */
public record ConditionFieldDto(
        String fieldName,
        FieldType fieldType,
        String displayName,
        List<String> allowedValues,
        String optionsEndpoint
) {}

