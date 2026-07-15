package org.datamate.authz.application.port.in.policy;

import org.datamate.authz.application.dto.policy.SavePoliciesRequestDto;

/**
 * Full-state sync for all policies belonging to a subject within this module.
 * Triggers OPA bundle recompilation on success.
 */
public interface SavePoliciesUseCase {
    void savePolicies(SavePoliciesRequestDto request);
}


