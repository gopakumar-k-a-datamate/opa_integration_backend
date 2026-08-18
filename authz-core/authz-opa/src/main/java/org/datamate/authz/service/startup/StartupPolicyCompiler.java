package org.datamate.authz.service.startup;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.authz.api.policy.PolicyCompiler;
import org.datamate.authz.api.policy.PermissionRepository;
import org.datamate.authz.model.policy.entity.Permission;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ensures OPA bundles are freshly compiled on application boot.
 * This is necessary because Flyway SQL migrations may have added or altered policies.
 */
@Component
public class StartupPolicyCompiler implements ApplicationListener<ContextRefreshedEvent> {

    @EnableLogger
    private Logger log;

    private final PermissionRepository permission;
    private final PolicyCompiler compiler;

    public StartupPolicyCompiler(PermissionRepository permission, PolicyCompiler compiler) {
        this.permission = permission;
        this.compiler = compiler;
    }

    private volatile boolean alreadyRan = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadyRan) return;
        alreadyRan = true;

        log.info("Starting OPA Bundle Recompilation from database state...");

        try {
            List<Permission> permissions = permission.findAllActive();
            Set<String> namespaces = permissions.stream()
                    .map(p -> p.getCode().split(":")[0])
                    .collect(Collectors.toSet());

            for (String namespace : namespaces) {
                compiler.recompile(namespace);
                log.info("Successfully recompiled OPA bundle for namespace: " + namespace);
            }
            
            log.info("Finished OPA Bundle Recompilation.");
        } catch (Exception e) {
            log.error("Failed to recompile OPA bundles on startup", e);
        }
    }
}
