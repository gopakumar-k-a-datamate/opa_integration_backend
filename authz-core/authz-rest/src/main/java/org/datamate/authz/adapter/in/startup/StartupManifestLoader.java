package org.datamate.authz.adapter.in.startup;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datamate.authz.application.port.out.policy.ConditionFieldPersistencePort;
import org.datamate.authz.application.port.out.policy.PermissionPersistencePort;
import org.datamate.authz.application.port.out.policy.PolicyPersistencePort;
import org.datamate.authz.application.port.out.policy.ResourcePersistencePort;
import org.datamate.authz.application.port.out.policy.PolicyCompilerPort;
import org.datamate.authz.domain.model.policy.entity.ConditionField;
import org.datamate.authz.domain.model.policy.entity.Permission;
import org.datamate.authz.domain.model.policy.entity.Policy;
import org.datamate.authz.domain.model.policy.entity.Resource;
import org.datamate.authz.shared.manifest.AuthzManifest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Inbound adapter that loads the auto-generated JSON manifest on startup.
 * Eliminates slow classpath scanning while perfectly syncing the database.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class StartupManifestLoader implements ApplicationListener<ContextRefreshedEvent> {

    private final ResourcePersistencePort resourcePort;
    private final PermissionPersistencePort permissionPort;
    private final ConditionFieldPersistencePort conditionFieldPort;
    private final PolicyPersistencePort policyPort;
    private final PolicyCompilerPort compilerPort;
    private final ObjectMapper objectMapper;

    @Value("${authz.sync.mode:MANIFEST}")
    private String syncMode;

    private volatile boolean alreadyRan = false;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadyRan) return;
        alreadyRan = true;

        if ("NONE".equalsIgnoreCase(syncMode)) {
            log.info("Authz sync disabled.");
            return;
        }

        try {
            ClassPathResource resource = new ClassPathResource("authz-manifest.json");
            if (!resource.exists()) {
                log.warn("No authz-manifest.json found! Skipping sync. Run maven build to generate it.");
                return;
            }

            try (InputStream is = resource.getInputStream()) {
                AuthzManifest manifest = objectMapper.readValue(is, AuthzManifest.class);
                Set<String> affectedNamespaces = new HashSet<>();

                for (AuthzManifest.ResourceManifest rm : manifest.getResources()) {
                    if (processManifestResource(rm)) {
                        affectedNamespaces.add(rm.getNamespace());
                    }
                }

                // Compile the bundle after all registrations via application port
                for (String namespace : affectedNamespaces) {
                    compilerPort.recompile(namespace);
                }
            }
        } catch (Exception e) {
            log.error("Failed to load authz-manifest.json", e);
        }
    }

    private boolean processManifestResource(AuthzManifest.ResourceManifest rm) {
        boolean hasChanged = false;
        String namespace = rm.getNamespace();
        String resourceName = rm.getName();
        String action = rm.getAction();
        String description = rm.getDescription();

        Optional<Resource> existingResource = resourcePort.findByNamespaceAndName(namespace, resourceName);
        if (existingResource.isEmpty() || !existingResource.get().getDescription().equals(description)) {
            hasChanged = true;
        }
        Resource resource = resourcePort.upsert(null, namespace, resourceName, description);

        String code = namespace + ":" + resourceName + ":" + action;
        Optional<Permission> existingPermission = permissionPort.findByCode(code);
        if (existingPermission.isEmpty() || !existingPermission.get().getDescription().equals(description)) {
            hasChanged = true;
        }
        Permission permission = permissionPort.upsert(null, resource.getId(), action, code, description);

        List<ConditionField> existingFields = conditionFieldPort.findAllByPermissionId(permission.getId());
        Set<String> incomingFieldNames = rm.getFields().stream()
                .map(AuthzManifest.FieldManifest::getFieldName)
                .collect(Collectors.toSet());

        if (upsertFields(permission.getId(), rm.getFields(), existingFields)) {
            hasChanged = true;
        }

        if (diffSyncRemovedFields(permission.getId(), incomingFieldNames, existingFields)) {
            hasChanged = true;
        }

        return hasChanged;
    }

    private boolean upsertFields(Long permissionId, List<AuthzManifest.FieldManifest> incomingFields, List<ConditionField> existingFields) {
        boolean hasChanged = false;
        for (AuthzManifest.FieldManifest fm : incomingFields) {
            boolean changed = existingFields.stream()
                    .noneMatch(f -> f.getFieldName().equals(fm.getFieldName())
                            && f.getFieldType() == fm.getType()
                            && f.getDisplayName().equals(fm.getDisplayName()));

            conditionFieldPort.upsert(null, permissionId, fm.getFieldName(),
                    fm.getType(), fm.getDisplayName(), fm.getAllowedValues(), fm.getOptionsEndpoint());
            
            if (changed) hasChanged = true;
        }
        return hasChanged;
    }

    private boolean diffSyncRemovedFields(Long permissionId, Set<String> incomingFieldNames, List<ConditionField> existingFields) {
        boolean hasChanged = false;
        for (ConditionField dbField : existingFields) {
            if (incomingFieldNames.contains(dbField.getFieldName())) continue;
            if (dbField.isDeprecated()) continue; // Already processed in a previous boot!

            hasChanged = true;
            List<Policy> affectedPolicies = policyPort.findEnabledReferencingField(
                    permissionId, dbField.getFieldName());

            if (!affectedPolicies.isEmpty()) {
                conditionFieldPort.markDeprecated(dbField.getId());
                for (Policy policy : affectedPolicies) {
                    policyPort.autoDisable(policy.getId(),
                            "Field '" + dbField.getFieldName() + "' was removed from code");
                }
            } else {
                conditionFieldPort.softDelete(dbField.getId());
            }
        }
        return hasChanged;
    }
}
