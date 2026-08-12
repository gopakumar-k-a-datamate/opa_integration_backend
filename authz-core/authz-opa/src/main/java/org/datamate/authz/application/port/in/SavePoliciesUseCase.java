package org.datamate.authz.application.port.in;

import org.datamate.authz.dto.policy.SavePoliciesRequest;

public interface SavePoliciesUseCase {
    void savePolicies(SavePoliciesRequest request);
}
