package org.datamate.authz.application.port.out.policy;

/**
 * Port for triggering OPA policy bundle recompilation.
 *
 * <p>Application use cases that modify policies (e.g. {@code SavePoliciesService})
 * depend on this interface rather than the concrete {@code PolicyCompilerService},
 * adhering to the Dependency Inversion Principle.</p>
 *
 * <p>The implementation reads all enabled policies from the database, generates
 * Rego code, packages it as {@code bundle.tar.gz}, and stores it in
 * {@code authz_policy_bundle_cache} for the OPA sidecar to pick up.</p>
 */
public interface PolicyCompilerPort {

    /**
     * Recompiles the OPA bundle from the current state of {@code authz_policy}.
     * Implementations must be thread-safe.
     */
    void recompile();
}


