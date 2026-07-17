package org.datamate.authz.application.port.in.policy;

import org.datamate.authz.application.dto.policy.PolicyGridItemDto;
import org.datamate.authz.domain.model.policy.enumtype.SubjectType;

import java.util.List;

/**
 * Returns the complete permission grid for a given subject (Role or User),
 * as consumed by the Admin UI's Role-Permission Grid.
 */
public interface GetPoliciesUseCase {
    List<PolicyGridItemDto> getPolicies(SubjectType subjectType, String subjectId, String namespace);
}


