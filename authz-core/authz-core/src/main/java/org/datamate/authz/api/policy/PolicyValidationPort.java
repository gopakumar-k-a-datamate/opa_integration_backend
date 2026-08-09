package org.datamate.authz.api.policy;

import org.datamate.authz.model.policy.valueobject.RegoValidationResult;

/**
 * Port for validating Rego syntax. The implementation delegates to
 * an external OPA instance via its REST API.
 */
public interface PolicyValidationPort {

    /**
     * Validates a raw Rego snippet for syntax correctness.
     *
     * <p>The implementation wraps the snippet in a temporary package,
     * sends it to OPA for parsing, and cleans up immediately after.</p>
     *
     * @param regoSnippet The raw Rego text (rule blocks only, no package/import).
     * @return Validation result with error locations if invalid.
     */
    RegoValidationResult validate(String regoSnippet);
}
