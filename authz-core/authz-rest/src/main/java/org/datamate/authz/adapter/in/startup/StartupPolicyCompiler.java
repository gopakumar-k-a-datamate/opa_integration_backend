package org.datamate.authz.adapter.in.startup;

import lombok.RequiredArgsConstructor;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.authz.application.port.out.policy.PolicyCompilerPort;
import org.datamate.authz.application.port.out.policy.ResourcePersistencePort;
import org.datamate.authz.domain.model.policy.entity.Resource;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ensures OPA bundles are freshly compiled on application boot.
 * This is necessary because Flyway SQL migrations may have added or altered policies.
 */
@RequiredArgsConstructor
@Component
public class StartupPolicyCompiler implements ApplicationListener<ContextRefreshedEvent> {

    @EnableLogger
    private Logger log;

    private final ResourcePersistencePort resourcePort;
    private final PolicyCompilerPort compilerPort;

    private volatile boolean alreadyRan = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadyRan) return;
        alreadyRan = true;

        log.info("Starting OPA Bundle Recompilation from database state...");

        try {
            List<Resource> resources = resourcePort.findAllActive();
            Set<String> namespaces = resources.stream()
                    .map(Resource::getNamespace)
                    .collect(Collectors.toSet());

            for (String namespace : namespaces) {
                compilerPort.recompile(namespace);
                log.info("Successfully recompiled OPA bundle for namespace: " + namespace);
            }
            
            log.info("Finished OPA Bundle Recompilation.");
        } catch (Exception e) {
            log.error("Failed to recompile OPA bundles on startup", e);
        }
    }
}
