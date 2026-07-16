package org.datamate.authz.application.port.in.policy;

import org.datamate.authz.application.dto.policy.ConditionFieldDto;

import java.util.List;

/**
 * Returns the list of ACTIVE condition fields for a given permission code.
 * Used by the Admin UI's Condition Builder to populate field dropdowns.
 */
public interface GetConditionFieldsUseCase {
    List<ConditionFieldDto> getFields(String permissionCode);
}


