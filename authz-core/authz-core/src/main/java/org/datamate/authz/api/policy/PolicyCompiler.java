package org.datamate.authz.api.policy;

/**
 *  for triggering policy bundle recompilation.
 *
 * <p>Application use cases that modify policies (e.g. {@code SavePoliciesService})
 * depend on this interface rather than a concrete compiler implementation,
 * adhering to the Dependency Inversion Principle.</p>
 *
 * <p>The implementation reads all enabled policies from the database, generates
 * the engine-specific code or format, packages it, and makes it available
 * for the policy engine sidecar or local evaluator to pick up.</p>
 */
public interface PolicyCompiler {

    /**
     * Recompiles the policy cache/bundle from the current state of {@code authz_policy} for a specific namespace.
     * Implementations must be thread-safe.
     */
    void recompile(String namespace);
}


