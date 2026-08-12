package org.datamate.authz.application.port.in;

import org.datamate.authz.dto.policy.PolicyGridItemDto;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import java.util.List;

public interface GetPoliciesUseCase {
    List<PolicyGridItemDto> getPolicies(SubjectType subjectType, String subjectId, String namespace);
}
