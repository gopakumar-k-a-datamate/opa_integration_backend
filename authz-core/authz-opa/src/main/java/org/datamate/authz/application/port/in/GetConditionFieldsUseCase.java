package org.datamate.authz.application.port.in;

import org.datamate.authz.dto.policy.ConditionFieldDto;
import java.util.List;

public interface GetConditionFieldsUseCase {
    List<ConditionFieldDto> getFields(String permissionCode);
}
